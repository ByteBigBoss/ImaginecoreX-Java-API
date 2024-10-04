package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.ChatStatus;
import entity.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author ByteBigBoss
 */
@WebServlet(name = "LoadChatHistory", urlPatterns = {"/LoadChatHistory"})
public class LoadChatHistory extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        //LoadChat?logged_user_id=1&other_user_id=2
        Gson gson = new Gson();

        Session session = HibernateUtil.getSessionFactory().openSession();

        String logged_user_id = req.getParameter("logged_user_id");
        String other_user_id = req.getParameter("other_user_id");

        //GET LOGGED USER
        User logged_user = (User) session.get(User.class, Integer.valueOf(logged_user_id));

        //GET OTHER USER
        User other_user = (User) session.get(User.class, Integer.valueOf(other_user_id));

        //GET CHATS
        Criteria getChats = session.createCriteria(Chat.class);
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
        List<Chat> chat_list = getChats.list();

        //GET CHAT STATUS = 1(SEEN)
        ChatStatus chatStatus = (ChatStatus) session.get(ChatStatus.class, 1);

        //CREATE CHAT ARRAY
        JsonArray chatArray = new JsonArray();

        //CREATE DATE TIME FORMAT
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a");

        for (Chat chat : chat_list) {

            //CREATE CHAT OBJECT
            JsonObject chatObject = new JsonObject();
            chatObject.addProperty("message", chat.getMessage());
            chatObject.addProperty("datetime", dateFormat.format(chat.getCreated_at()));

            //GET CHATS ONLY FROM OTHER USER
            if (chat.getFromUser().getId() == other_user.getId()) {

                //ADD SITE TO CHAT OBJECT
                chatObject.addProperty("side", "left");

                //GET ONLY UNSEEN CHATS (CHAT STATUS = 2)
                if (chat.getChatStatus().getId() == 2) {
                    chat.setChatStatus(chatStatus);
                    session.update(chat);
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

        //UPDATE DB
        session.beginTransaction().commit();

        //SEND RESPONSE
        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(chatArray));

    }

}
