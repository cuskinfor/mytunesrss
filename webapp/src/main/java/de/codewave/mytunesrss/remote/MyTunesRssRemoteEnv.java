package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class MyTunesRssRemoteEnv {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssRemoteEnv.class);

    private static final ThreadLocal<HttpServletRequest> THREAD_REQUESTS = new ThreadLocal<HttpServletRequest>();

    private static final ThreadLocal<Session> THREAD_SESSIONS = new ThreadLocal<Session>();

    private static final Session DUMMY_SESSION = new Session(null, null, 0);

    public static HttpServletRequest getRequest() {
        return THREAD_REQUESTS.get();
    }

    public static void setRequest(HttpServletRequest request) {
        THREAD_REQUESTS.set(request);
        String sid = getRequest().getHeader("X-MyTunesRSS-ID");
        if (StringUtils.isEmpty(sid)) {
            sid = getRequest().getPathInfo();
        }
        if (StringUtils.isNotEmpty(sid) && sid.length() > 1) {
            if (sid.startsWith("/")) {
                sid = sid.substring(1);
            }
            THREAD_SESSIONS.set(RemoteApiSessionManager.getInstance(request).getSession(sid));
            LOG.debug("Received remote API call for session \"" + sid + "\".");
        }
    }

    public static void removeRequest() {
        THREAD_SESSIONS.remove();
        THREAD_REQUESTS.remove();
    }

    public static Session getSession() {
        Session session = THREAD_SESSIONS.get();
        return session != null ? session : DUMMY_SESSION;
    }

    public static String getServerCall(MyTunesRssCommand command, String pathInfo) {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        HttpServletRequest request = MyTunesRssRemoteEnv.getRequest();
        String auth = MyTunesRssWebUtils.encryptPathInfo(request,
                                                         "auth=" + MyTunesRssBase64Utils.encode(user.getName()) + " " + MyTunesRssBase64Utils.encode(
                                                                 user.getPasswordHash()));
        String url = MyTunesRssWebUtils.getServletUrl(request) + "/" + command.getName() + "/" + auth;
        if (StringUtils.isNotEmpty(pathInfo)) {
            url += "/" + MyTunesRssWebUtils.encryptPathInfo(request, pathInfo);
        }
        return url;
    }

    public static void addSession(Session session) {
        LOG.debug("Adding remote API session with user \"" + session.getUser().getName() + "\" and \"" + session.getId() + "\".");
        RemoteApiSessionManager.getInstance(getRequest()).addSession(session.getId(), session);
    }
}