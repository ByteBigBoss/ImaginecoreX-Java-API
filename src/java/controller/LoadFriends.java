package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.Friend;
import entity.User;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.CheckAvatar;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author ByteBigBoss
 */
@WebServlet(name = "LoadFriends", urlPatterns = {"/LoadFriends"})
public class LoadFriends extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject resObj = new JsonObject();
        resObj.addProperty("success", false);

        try {

            Session session = HibernateUtil.getSessionFactory().openSession();

            //GET USER ID FROM REQUEST PARAMETERS
            String userId = req.getParameter("id");

            //GET USER OBJECT
            User user = (User) session.get(User.class, Integer.valueOf(userId));

            //LOAD ALL USER FRIENDS
            Criteria getFriendList = session.createCriteria(Friend.class);
            getFriendList.add(
                    Restrictions.or(
                            Restrictions.eq("activeUser", user),
                            Restrictions.eq("friend", user)
                    ));
            getFriendList.addOrder(Order.asc("id"));

            List<Friend> friendList = getFriendList.list();

            JsonArray friendArr = new JsonArray();

            CheckAvatar checkAvatar = new CheckAvatar();

            for (Friend friend : friendList) {

                JsonObject friendObj = new JsonObject();

                if (friend.getActiveUser().equals(user)) {
                    friend.setActiveUser(null);
                    

                    if (checkAvatar.check(friend.getFriend(), req)) {
                        friendObj.addProperty("avatar", true);
                    } else {
                        friendObj.addProperty("avatar", false);
                    }
                   

                } else if (friend.getFriend().equals(user)) {
                    friend.setFriend(null);

                    if (checkAvatar.check(friend.getActiveUser(), req)) {
                        friendObj.addProperty("avatar", true);
                    } else {
                        friendObj.addProperty("avatar", false);
                    }
                }
                
                friendObj.add("data", gson.toJsonTree(friend));
                friendArr.add(friendObj);

            }

            resObj.add("friend_list", gson.toJsonTree(friendArr));
            resObj.addProperty("success", true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(resObj));
    }

}
