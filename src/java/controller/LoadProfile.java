package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.Friend;
import entity.Moment;
import entity.Notification;
import entity.Profile;
import entity.SavedMoment;
import entity.User;
import java.io.IOException;
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
@WebServlet(name = "LoadProfile", urlPatterns = {"/LoadProfile"})
public class LoadProfile extends HttpServlet {

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

            //LOAD PROFILE
            Criteria getUserProfile = session.createCriteria(Profile.class);
            getUserProfile.add(Restrictions.eq("user", user));
            Profile userProfile = (Profile) getUserProfile.uniqueResult();

            //LOAD USER MOMENTS
            Criteria getUserMoments = session.createCriteria(Moment.class);
            getUserMoments.add(Restrictions.eq("user", user));
            getUserMoments.addOrder(Order.desc("id"));


            //RESPONSE
            resObj.addProperty("profile", gson.toJson(userProfile));

        } catch (Exception e) {
            e.printStackTrace();
        }

        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(resObj));
    }

}
