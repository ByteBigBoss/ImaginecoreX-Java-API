package model;

import entity.User;
import java.io.File;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ByteBigBoss
 * @org ImaginecoreX
 */
public class CheckAvatar {

    public boolean check(User user, HttpServletRequest req) {

        //CHECK AVATAR IMAGE
        //APPLICATION PATH
        String serverPath = req.getServletContext().getRealPath("");
        String folderPath = serverPath.replace("build" + File.separator + "web", "web");
        String otherUserAvatarPath = folderPath + File.separator + "user" + File.separator + user.getMobile() + File.separator + "avatar.png";
        File otherUserAvatarFile = new File(otherUserAvatarPath);

        if (otherUserAvatarFile.exists()) {
            //AVATAR IMAGE FOUND
            return true;
        } else {
            //AVATAR IMAGE NOT FOUND
            return false;
        }

    }

}
