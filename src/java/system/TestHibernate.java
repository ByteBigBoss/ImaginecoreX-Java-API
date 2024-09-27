package system;

import entity.ChatStatus;
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
@WebServlet(name="TestHibernate", urlPatterns={"/TestHibernate"})
public class TestHibernate extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        try {
            
            Session session = HibernateUtil.getSessionFactory().openSession();
            
            ChatStatus chatStatus = new ChatStatus();
            chatStatus.setName("Seen");
            
            session.save(chatStatus);
            session.beginTransaction().commit();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

}
