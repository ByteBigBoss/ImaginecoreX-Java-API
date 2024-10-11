package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.ChatStatus;
import entity.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author ByteBigBoss
 * @org ImaginecoreX
 */
@ServerEndpoint("/SocketEntry")
public class Socket {

    private Gson gson = new Gson();

    private static final HashMap<String, Session> userSessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        System.out.println("Connection Opened");
   
      
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {

        JsonObject incomingMsg = gson.fromJson(message, JsonObject.class);
        String action = incomingMsg.get("action").getAsString();

        String logged_user_id = incomingMsg.get("logged_user_id").getAsString();

        userSessions.put(logged_user_id, session);

        if (incomingMsg.has("action")) {
            switch (action) {
                case "SendChat":
                    sendChat(incomingMsg, session);

                    //BROADCAST TO BOTH USERS
                    broadcastChatUpdate(incomingMsg);
                    break;
                case "LoadChatHistory":
                    System.out.println("LOADING CHAT HISTORY:: =======================================");
                    loadChatHistory(incomingMsg, session);
                    break;
            }
        } else {
            System.out.println("Unexpected message format: " + message);
        }

    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Session closed with ID: " + session.getId());

        //REMOVE THE SESSION FROM THE MAP WHEN CLOSED
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    @OnError
    public void onError(Session session, Throwable t) {
        t.printStackTrace();
    }

    private void broadcastChatUpdate(JsonObject incomingMsg) {
        String logged_user_id = incomingMsg.get("logged_user_id").getAsString();
        String other_user_id = incomingMsg.get("other_user_id").getAsString();

        try {

            String boradcastMsg = gson.toJson(incomingMsg);

            Session sender = userSessions.get(logged_user_id);
            if (sender != null && sender.isOpen()) {
                sender.getBasicRemote().sendText(boradcastMsg);
            }

            Session receiver = userSessions.get(other_user_id);
            if (receiver != null && receiver.isOpen()) {
                receiver.getBasicRemote().sendText(boradcastMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendChat(JsonObject incomingMsg, Session socketSession) {

        Gson gson = new Gson();

        String logged_user_id = incomingMsg.get("logged_user_id").getAsString();
        String other_user_id = incomingMsg.get("other_user_id").getAsString();
        String message = incomingMsg.get("message").getAsString();

        try {

            org.hibernate.Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            hibernateSession.beginTransaction();

            //GET LOGGED USER
            User logged_user = (User) hibernateSession.get(User.class, Integer.valueOf(logged_user_id));

            //GET OTHER USER
            User other_user = (User) hibernateSession.get(User.class, Integer.valueOf(other_user_id));

            //GET CHAT STATUS => 2=SEEN
            ChatStatus chatStatus = (ChatStatus) hibernateSession.get(ChatStatus.class, 2);

            //SAVE CHAT
            entity.Chat chat = new entity.Chat();
            chat.setChatStatus(chatStatus);
            chat.setFromUser(logged_user);
            chat.setToUser(other_user);
            chat.setMessage(message);

            //SAVE TO MEMORY
            hibernateSession.save(chat);
            hibernateSession.getTransaction().commit();

            //NOTIFY USER
            JsonObject chatMessage = new JsonObject();
            chatMessage.addProperty("message", message);
            chatMessage.addProperty("fromUserId", logged_user_id);
            chatMessage.addProperty("toUserId", other_user_id);

            socketSession.getBasicRemote().sendText(gson.toJson(chatMessage));

            hibernateSession.close();
            socketSession.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadChatHistory(JsonObject incomingMsg, Session socketSession) {
        String logged_user_id = incomingMsg.get("logged_user_id").getAsString();
        String other_user_id = incomingMsg.get("other_user_id").getAsString();

        org.hibernate.Session hibernateSession = null;

        Gson gson = new Gson();

        //CREATE CHAT ARRAY
        JsonArray chatArray = new JsonArray();
        try {

            hibernateSession = HibernateUtil.getSessionFactory().openSession();
            hibernateSession.beginTransaction();

            //GET LOGGED USER
            User logged_user = (User) hibernateSession.get(User.class, Integer.valueOf(logged_user_id));

            //GET OTHER USER
            User other_user = (User) hibernateSession.get(User.class, Integer.valueOf(other_user_id));

            //GET CHATS
            Criteria getChats = hibernateSession.createCriteria(entity.Chat.class);
            getChats.add(Restrictions.or(
                    Restrictions.and(
                            Restrictions.eq("fromUser", logged_user),
                            Restrictions.eq("toUser", other_user)
                    ),
                    Restrictions.and(
                            Restrictions.eq("fromUser", other_user),
                            Restrictions.eq("toUser", logged_user)
                    )
            ));

            //SORT CHATS
            getChats.addOrder(Order.asc("created_at"));

            //GET CHAT LIST
            List<entity.Chat> chat_list = getChats.list();

            //GET CHAT STATUS = 1(SEEN)
            ChatStatus chatStatus = (ChatStatus) hibernateSession.get(ChatStatus.class, 1);

            //CREATE DATE TIME FORMAT
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a");

            for (entity.Chat chat : chat_list) {

                //CREATE CHAT OBJECT
                JsonObject chatObject = new JsonObject();
                chatObject.addProperty("message", chat.getMessage());
                chatObject.addProperty("datetime", String.valueOf(dateFormat.format(chat.getCreated_at())));

                //GET CHATS ONLY FROM OTHER USER
                if (chat.getFromUser().getId() == other_user.getId()) {

                    //ADD SITE TO CHAT OBJECT
                    chatObject.addProperty("side", "left");

                    //GET ONLY UNSEEN CHATS (CHAT STATUS = 2)
                    if (chat.getChatStatus().getId() == 2) {
                        chat.setChatStatus(chatStatus);
                        hibernateSession.update(chat);
                    }

                } else {
                    //GET CHAT FROM LOGGED USER

                    //ADD SITE TO CHAT OBJECT
                    chatObject.addProperty("side", "right");

                    chatObject.addProperty("status", chat.getChatStatus().getId());//1=>SEEN, 2=>UNSEEN
                }

                //ADD CHAT OBJECT INTO CHAT ARRAY
                chatArray.add(chatObject);
            }

            hibernateSession.getTransaction().commit();
            socketSession.getBasicRemote().sendText(gson.toJson(chatArray));
            socketSession.close();
            hibernateSession.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (hibernateSession != null && hibernateSession.getTransaction().isActive()) {
                hibernateSession.getTransaction().rollback();
            }
        } finally {
            if (hibernateSession != null) {
                hibernateSession.close();
            }
        }
    }

}
