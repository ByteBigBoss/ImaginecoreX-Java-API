package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.User;
import entity.UserStatus;
import java.io.File;
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
@WebServlet(name = "LoadChatData", urlPatterns = {"/LoadChatData"})
public class LoadChatData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject resObj = new JsonObject();
        resObj.addProperty("success", false);
        resObj.addProperty("message", "Unable to fetch your data.");

        try {
            Session session = HibernateUtil.getSessionFactory().openSession();

            //GET USER ID FROM REQUEST PARAMETERS
            String userId = req.getParameter("id");

            //GET USER OBJECT
            User user = (User) session.get(User.class, Integer.parseInt(userId));

            //GET USER STATUS = 1 (ONLINE)
            UserStatus userStatus = (UserStatus) session.get(UserStatus.class, 1);

            //UPDATE USER STATUS
            user.setUserStatus(userStatus);
            session.update(user);

            //GET OTHER USERS
            Criteria findUsers = session.createCriteria(User.class);
            findUsers.add(Restrictions.ne("id", user.getId()));

            List<User> otherUserList = findUsers.list();

            JsonArray jsonChatArray = new JsonArray();

            //GET OTEHR USER ONE BY ONE
            for (User otherUser : otherUserList) {

                //GET LAST CONVERSATION
                Criteria getChatList = session.createCriteria(Chat.class);
                getChatList.add(
                        Restrictions.or(
                                Restrictions.and(
                                        Restrictions.eq("fromUser", user),
                                        Restrictions.eq("toUser", otherUser)
                                ),
                                Restrictions.and(
                                        Restrictions.eq("fromUser", otherUser),
                                        Restrictions.eq("toUser", user)
                                )
                        ));
                getChatList.addOrder(Order.desc("id"));
                getChatList.setMaxResults(1);

                //CREATE CHAT ITEM JSOM TPP SEMD FRPMTEMD DATA
                JsonObject jsonChatItem = new JsonObject();
                jsonChatItem.addProperty("other_user_id", otherUser.getId());
                jsonChatItem.addProperty("other_user_mobile", otherUser.getMobile());
                jsonChatItem.addProperty("other_user_name", otherUser.getFirstName() + " " + otherUser.getLastName());
                jsonChatItem.addProperty("other_user_status", otherUser.getUserStatus().getId());// 1=>ONLINE || 2=>OFFLINE

                //CHECK AVATAR IMAGE
                //APPLICATION PATH
                String serverPath = req.getServletContext().getRealPath("");
                String folderPath = serverPath.replace("build" + File.separator + "web", "web");
                String otherUserAvatarPath = folderPath + File.separator + "user" + File.separator + otherUser.getMobile() + File.separator + "avatar.png";
                File otherUserAvatarFile = new File(otherUserAvatarPath);

                if (otherUserAvatarFile.exists()) {
                    //AVATAR IMAGE FOUND
                    jsonChatItem.addProperty("avatar_image_found", true);
                } else {
                    //AVATAR IMAGE NOT FOUND
                    jsonChatItem.addProperty("avatar_image_found", false);
                    jsonChatItem.addProperty("other_user_avatar_letters", otherUser.getFirstName().charAt(0) + "" + otherUser.getLastName().charAt(0));
                }

                //GET CHAT LIST
                List<Chat> dbChatList = getChatList.list();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

                if (dbChatList.isEmpty()) {
                    //NO CHAT
                    jsonChatItem.addProperty("message", "Let's Start New Conversation");
                    jsonChatItem.addProperty("dateTime", String.valueOf(dateFormat.format(otherUser.getCreated_at())));
                    jsonChatItem.addProperty("chat_status_id", 1); // 1=> SEEN || 2=>UNSEEN
                } else {
                    //FOUND LAST CHAT
                    jsonChatItem.addProperty("message", dbChatList.get(0).getMessage());
                    jsonChatItem.addProperty("dateTime", String.valueOf(dateFormat.format(dbChatList.get(0).getCreated_at())));
                    jsonChatItem.addProperty("chat_status_id", dbChatList.get(0).getChatStatus().getId());

                }

                otherUser.setPassword(null);
                jsonChatArray.add(jsonChatItem);

            }

            //SEND USERS
            resObj.addProperty("success", true);
            resObj.addProperty("message", "success");
//            resObj.add("user", gson.toJsonTree(user));
            resObj.add("jsonChatArray", gson.toJsonTree(jsonChatArray));

            session.beginTransaction().commit();
            session.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(resObj));
    }

}
