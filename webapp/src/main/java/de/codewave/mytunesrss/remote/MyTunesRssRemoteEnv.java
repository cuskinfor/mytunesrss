package de.codewave.mytunesrss.remote;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.remote.render.*;

public class MyTunesRssRemoteEnv {
    private static final ThreadLocal<User> USERS = new ThreadLocal<User>();
    private static final ThreadLocal<HttpServletRequest> REQUESTS = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<RenderMachine> RENDER_MACHINES = new ThreadLocal<RenderMachine>();

    public static User getUser() {
        return USERS.get();
    }

    public static void setUser(User user) {
        USERS.set(user);
    }

    public static void removeUser() {
        USERS.remove();
    }

    public static HttpServletRequest getRequest() {
        return REQUESTS.get();
    }

    public static void setRequest(HttpServletRequest request) {
        REQUESTS.set(request);
    }

    public static void removeRequest() {
        REQUESTS.remove();
    }

    public static RenderMachine getRenderMachine() {
        return RENDER_MACHINES.get();
    }

    public static void setRenderMachine(RenderMachine renderMachine) {
        RENDER_MACHINES.set(renderMachine);
    }

    public static void removeRenderMachine() {
        RENDER_MACHINES.remove();
    }

    public static String getServerCall(MyTunesRssCommand command, String pathInfo) {
        User user = MyTunesRssRemoteEnv.getUser();
        HttpServletRequest request = MyTunesRssRemoteEnv.getRequest();
        String auth = MyTunesRssWebUtils.encryptPathInfo("auth=" + MyTunesRssBase64Utils.encode(user.getName()) + " " +
                MyTunesRssBase64Utils.encode(user.getPasswordHash()));
        String url = MyTunesRssWebUtils.getServletUrl(request) + "/" + command.getName() + "/" + auth;
        if (StringUtils.isNotEmpty(pathInfo)) {
            url += "/" + MyTunesRssWebUtils.encryptPathInfo(pathInfo);
        }
        return url;
    }
}