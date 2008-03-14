package de.codewave.mytunesrss.remote;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyTunesRssRemoteEnv {
    private static final Log LOG = LogFactory.getLog(MyTunesRssRemoteEnv.class);

    private static final ThreadLocal<HttpServletRequest> THREAD_REQUESTS = new ThreadLocal<HttpServletRequest>();

    private static final ThreadLocal<Session> THREAD_SESSIONS = new ThreadLocal<Session>();

    private static final Map<String, Session> SESSIONS = new HashMap<String, Session>();

    private static final Session DUMMY_SESSION = new Session(null, null, 0);

    static {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            public void run() {
                LOG.debug("Purging remote API sessions.");
                synchronized (SESSIONS) {
                    for (Iterator<Map.Entry<String, Session>> iter = SESSIONS.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry<String, Session> entry = iter.next();
                        if (entry.getValue().isExpired()) {
                            iter.remove();
                        }
                    }
                }
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    public static HttpServletRequest getRequest() {
        return THREAD_REQUESTS.get();
    }

    public static void setRequest(HttpServletRequest request) {
        THREAD_REQUESTS.set(request);
        String sid = getRequest().getPathInfo();
        if (StringUtils.isNotEmpty(sid) && sid.length() > 1) {
            sid = sid.substring(1);
            synchronized (SESSIONS) {
                if (SESSIONS.containsKey(sid)) {
                    LOG.debug("Received remote API call for session \"" + sid.substring(1) + "\".");
                    THREAD_SESSIONS.set(SESSIONS.get(sid));
                }
            }
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
        String auth = MyTunesRssWebUtils.encryptPathInfo("auth=" + MyTunesRssBase64Utils.encode(user.getName()) + " " + MyTunesRssBase64Utils.encode(
                user.getPasswordHash()));
        String url = MyTunesRssWebUtils.getServletUrl(request) + "/" + command.getName() + "/" + auth;
        if (StringUtils.isNotEmpty(pathInfo)) {
            url += "/" + MyTunesRssWebUtils.encryptPathInfo(pathInfo);
        }
        return url;
    }

    public static void addSession(Session session) {
        synchronized (SESSIONS) {
            LOG.debug("Adding remote API session with user \"" + session.getUser().getName() + "\" and \"" + session.getId() + "\".");
            SESSIONS.put(session.getId(), session);
        }
    }
}