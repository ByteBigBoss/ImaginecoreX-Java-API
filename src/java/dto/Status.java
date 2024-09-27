package dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ByteBigBoss
 * @org ImaginecoreX
 */
public class Status implements Serializable {

    private int status;
    private List<String> servlets;
    private String msg;
    private String appName;
    private Date time;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Status() {
    }

    public Status(int status, List<String> servlets, String msg) {
        this.status = status;
        this.servlets = servlets;
        this.msg = msg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getServlets() {
        return servlets;
    }

    public void setServlets(List<String> servlets) {
        this.servlets = servlets;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
