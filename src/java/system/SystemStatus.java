package system;

import com.bytebigboss.bcors.Bcors;
import com.google.gson.Gson;
import dto.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ByteBigBoss
 */
@WebServlet(name = "SystemStatus", urlPatterns = {"/SystemStatus"})
public class SystemStatus extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        

//        req.getSession();
        Bcors.setCors(req, res);
        Bcors.getInstance().setAllowedOrigins(Arrays.asList("exp://192.168.8.185:8081"));
        
        Gson gson = new Gson();

        Status status = new Status();
        status.setStatus(205);
        status.setMsg("All Systems Up To Date.");
        status.setAppName("ImaginecoreX");
        status.setTime(new Date());

        List<String> ls = new ArrayList();
        ls.add("App#Member @ ImaginecoreX");        
        ls.add("SignUp#Member @ ImaginecoreX");

        
        status.setServlets(ls);

        res.getWriter().write(gson.toJson(status));
        System.out.println(gson.toJson(status));

    }

    
}
