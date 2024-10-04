package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author ByteBigBoss
 */
@WebServlet(name = "GetLetters", urlPatterns = {"/GetLetters"})
public class GetLetters extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject resObj = new JsonObject();
        resObj.addProperty("letters", "");

        Session session = HibernateUtil.getSessionFactory().openSession();

        String mobile = req.getParameter("mobile");

        Criteria findUser = session.createCriteria(User.class);
        findUser.add(Restrictions.eq("mobile", mobile));

        if (!findUser.list().isEmpty()) {
            //USER FOUND
            User user = (User) findUser.uniqueResult();
            String letters = user.getFirstName().charAt(0) + "" + user.getLastName().charAt(0);

            resObj.addProperty("letters", letters);
        }

        session.close();

        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(resObj));

    }

}
