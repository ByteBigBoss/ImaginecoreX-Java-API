package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.UserStatus;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import utils.Validator;

/**
 *
 * @author ByteBigBoss
 */
@MultipartConfig
@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject resJson = new JsonObject();
        resJson.addProperty("success", false);

        JsonObject reqJsonObject = gson.fromJson(req.getReader(), JsonObject.class);
        String mobile = reqJsonObject.get("mobile").getAsString();
        String password = reqJsonObject.get("password").getAsString();

        if (mobile.isEmpty()) {
            //MOBILE NUMBER IS BLANK
            resJson.addProperty("target", "mobile");
            resJson.addProperty("message", "Please Fill Your Mobile Number");

        } else if (!Validator.VALIDATE_MOBILE(mobile)) {
            //INVALID MOBILE NUMBER
            resJson.addProperty("target", "mobile");
            resJson.addProperty("message", "Invalid Mobile Number");

        } else if (password.isEmpty()) {
            //PASSWORD IS BLANK
            resJson.addProperty("target", "password");
            resJson.addProperty("message", "Please Fill Your Password");

        } else if (!Validator.VALIDATE_PASSWORD(password)) {
            //INVALID PASSWORD
            resJson.addProperty("target", "password");
            resJson.addProperty("message", "Invalid Password");

        } else {
            //VALIDATION COMPLETED

            Session session = HibernateUtil.getSessionFactory().openSession();

            //SEARCH MOBILE NUMBER AND PASSWORD
            Criteria findUser = session.createCriteria(User.class);
            findUser.add(Restrictions.eq("mobile", mobile));
            findUser.add(Restrictions.eq("password", password));

            if (!findUser.list().isEmpty()) {
                //MOBILE NUMBER FOUND
                User user = (User) findUser.uniqueResult();

                resJson.addProperty("success", true);
                resJson.addProperty("message", "Sign In Success");
                resJson.add("user", gson.toJsonTree(user));

            } else {
                //USER NOT FOUND
                resJson.addProperty("target", "both");
                resJson.addProperty("message", "Invalid Credentials!.");
            }

            session.close();

        }

        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(resJson));

    }

}
