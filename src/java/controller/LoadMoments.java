package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Friend;
import entity.Moment;
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

/**
 *
 * @author ByteBigBoss
 */
@WebServlet(name="LoadMoments", urlPatterns={"/LoadMoments"})
public class LoadMoments extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        Gson gson = new Gson();

        JsonObject resObj = new JsonObject();
        resObj.addProperty("success", false);
        
        try {
            
            Session session = HibernateUtil.getSessionFactory().openSession();
            
            //GET ALL MOMENTS
            Criteria getMomentList = session.createCriteria(Moment.class);
            getMomentList.addOrder(Order.desc("id"));
            
            List<Moment> momentList = getMomentList.list();
            
             resObj.add("moment_list", gson.toJsonTree(momentList));
            resObj.addProperty("success", true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
     
        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(resObj));
    }
}
