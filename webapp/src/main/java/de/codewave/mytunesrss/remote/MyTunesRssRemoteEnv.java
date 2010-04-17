package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class MyTunesRssRemoteEnv {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssRemoteEnv.class);

    private static final ThreadLocal<HttpServletRequest> THREAD_REQUESTS = new ThreadLocal<HttpServletRequest>();

    private static final ThreadLocal<Session> THREAD_SESSIONS = new ThreadLocal<Session>();

    private static Session getDummySession() {
        return new Session(null, null, 0);
    }

    public static HttpServletRequest getRequest() {
        return THREAD_REQUESTS.get();
    }

    public static void setRequest(HttpServletRequest request) {
        THREAD_REQUESTS.set(request);
        request.setAttribute("downloadPlaybackServletUrl", MyTunesRssWebUtils.getServletUrl(request));
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
            request.setAttribute("auth", getAuth());
        }
    }

    public static void removeRequest() {
        THREAD_SESSIONS.remove();
        THREAD_REQUESTS.remove();
    }

    public static Session getSession() {
        Session session = THREAD_SESSIONS.get();
        return session != null ? session : getDummySession();
    }

    public static String getServerCall(MyTunesRssCommand command, String pathInfo) {
        HttpServletRequest request = MyTunesRssRemoteEnv.getRequest();
        String auth = MyTunesRssWebUtils.encryptPathInfo(request, getAuth());
        String url = MyTunesRssWebUtils.getServletUrl(request) + "/" + command.getName() + "/" + auth;
        if (StringUtils.isNotEmpty(pathInfo)) {
            url += "/" + MyTunesRssWebUtils.encryptPathInfo(request, pathInfo);
        }
        return url;
    }

    private static String getAuth() {
        User user = getSession().getUser();
        if (user != null) {
            return "auth=" + MyTunesRssBase64Utils.encode(user.getName()) + " " + MyTunesRssBase64Utils.encode(
                    user.getPasswordHash());
        }
        return null;
    }

    public static void addSession(HttpServletRequest request, Session session) {
        LOG.debug("Adding remote API session with user \"" + session.getUser().getName() + "\" and \"" + session.getId() + "\".");
        RemoteApiSessionManager.getInstance(request).addSession(session.getId(), session);
        THREAD_SESSIONS.set(session);
    }

    public static void addOrTouchSessionForRegularSession(HttpServletRequest request, User user) {
        String sid = (String) request.getSession().getAttribute("remoteApiSessionId");
        if (StringUtils.isBlank(sid)) {
            sid = createSessionId();
            LOG.debug("Adding remote API session with user \"" + user.getName() + "\" and \"" + sid + "\"");
            addSession(request, new Session(sid, user, user.getSessionTimeout() * 60000));
            request.getSession().setAttribute("remoteApiSessionId", sid);
        } else {
            RemoteApiSessionManager.getInstance(request).getSession(sid);
        }
    }

    public static Session getSessionForRegularSession(HttpServletRequest request) {
        String sid = (String) request.getSession().getAttribute("remoteApiSessionId");
        Session session = RemoteApiSessionManager.getInstance(request).getSession(sid);
        return session != null ? session : getDummySession();
    }

    public static String createSessionId() {
        return new String(Hex.encodeHex(MyTunesRss.MD5_DIGEST.digest(MyTunesRssUtils.getUtf8Bytes(UUID.randomUUID().toString() + System.currentTimeMillis()))));
    }
}