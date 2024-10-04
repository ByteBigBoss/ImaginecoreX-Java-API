package controller;

import com.bytebigboss.bcors.Bcors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import entity.UserStatus;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
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
@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject resJson = new JsonObject();
        resJson.addProperty("success", false);

        String fname = req.getParameter("fname");
        String lname = req.getParameter("lname");
        String mobile = req.getParameter("mobile");
        String password = req.getParameter("password");
        Part avatar = req.getPart("avatar");

        if (fname.isEmpty()) {
            //FIRST NAME IS BLANK
            resJson.addProperty("target", "fname");
            resJson.addProperty("message", "Please Fill Your First Name");

        } else if (lname.isEmpty()) {
            //LAST NAME IS BLANK
            resJson.addProperty("target", "lname");
            resJson.addProperty("message", "Please Fill Your Last Name");

        } else if (mobile.isEmpty()) {
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

            //SEARCH MOBILE NUMBER
            Criteria findUser = session.createCriteria(User.class);
            findUser.add(Restrictions.eq("mobile", mobile));

            if (!findUser.list().isEmpty()) {
                //MOBILE NUMBER FOUND
                resJson.addProperty("target", "mobile");
                resJson.addProperty("message", "Mobile Number Already Used.");
            } else {
                //MOBILE NUMBER NOT FOUND
                User user = new User();
                user.setFirstName(fname);
                user.setLastName(lname);
                user.setMobile(mobile);
                user.setPassword(password);

                //GET USER STATUS 2 = Ofline
                UserStatus userStatus = (UserStatus) session.get(UserStatus.class, 2);
                user.setUserStatus(userStatus);

                session.save(user);
                session.beginTransaction().commit();

                resJson.addProperty("success", true);
                resJson.addProperty("message", "User Registration Complete!");

                //CHECK UPLOADED IMAGE
                if (avatar != null) {
                    //IMAGE SELECTED

                    //APPLICATION PATH
                    String applicationPath = req.getServletContext().getRealPath("");
                    String newAplicationPath = applicationPath.replace("build" + File.separator + "web", "web");

                    //USER FOLDER
                    File userFolder = new File(newAplicationPath + File.separator + "user" + File.separator + mobile);
                    userFolder.mkdir();

                    //NEW FILE
                    File file = new File(userFolder, "avatar.png");
                    Files.copy(avatar.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            session.close();

        }

        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(resJson));

    }

}
