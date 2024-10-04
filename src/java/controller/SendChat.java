package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Chat;
import entity.ChatStatus;
import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Session;

/**
 *
 * @author ByteBigBoss
 */
@WebServlet(name = "SendChat", urlPatterns = {"/SendChat"})
public class SendChat extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //LoadChat?logged_user_id=1&other_user_id=2
        Gson gson = new Gson();

        JsonObject resObj = new JsonObject();
        resObj.addProperty("success", false);

        try {
            Session session = HibernateUtil.getSessionFactory().openSession();

            String logged_user_id = req.getParameter("logged_user_id");
            String other_user_id = req.getParameter("other_user_id");
            String message = req.getParameter("message");

            //GET LOGGED USER
            User logged_user = (User) session.get(User.class, Integer.valueOf(logged_user_id));

            //GET OTHER USER
            User other_user = (User) session.get(User.class, Integer.valueOf(other_user_id));

            //GET CHAT STATUS => 2=SEEN
            ChatStatus chatStatus = (ChatStatus) session.get(ChatStatus.class, 2);

            //SAVE CHAT
            Chat chat = new Chat();
            chat.setChatStatus(chatStatus);
            chat.setFromUser(logged_user);
            chat.setToUser(other_user);
            chat.setMessage(message);

            //SAVE TO MEMORY
            session.save(chat);
            //SAVE TO DB
            session.beginTransaction().commit();

            resObj.addProperty("success", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //SEND RESPONSE
        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(resObj));

    }

}
