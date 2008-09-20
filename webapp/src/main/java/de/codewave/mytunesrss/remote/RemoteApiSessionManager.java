package de.codewave.mytunesrss.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * de.codewave.mytunesrss.remote.RemoteApiSessionManager
 */
public class RemoteApiSessionManager implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteApiSessionManager.class);

    private final Map<String, Session> mySessions = new HashMap<String, Session>();

    private ScheduledExecutorService myExecutorService;

    public void contextInitialized(ServletContextEvent sce) {
        myExecutorService = Executors.newSingleThreadScheduledExecutor();
        myExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                LOGGER.debug("Purging remote API sessions.");
                synchronized (mySessions) {
                    for (Iterator<Map.Entry<String, Session>> iter = mySessions.entrySet().iterator(); iter.hasNext();) {
                        Map.Entry<String, Session> entry = iter.next();
                        if (entry.getValue().isExpired()) {
                            LOGGER.debug("Removing expired session \"" + entry.getValue().getId() + "\" of user \"" +
                                    entry.getValue().getUser().getName() + "\".");
                            iter.remove();
                        }
                    }
                }
            }
        }, 0, 60, TimeUnit.SECONDS);
        sce.getServletContext().setAttribute(getClass().getCanonicalName() + ".INSTANCE", this);
        LOGGER.debug("Started remote API session manager.");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        myExecutorService.shutdownNow();
        LOGGER.debug("Stopped remote API session manager.");
    }

    public static RemoteApiSessionManager getInstance(HttpServletRequest request) {
        return (RemoteApiSessionManager)request.getSession().getServletContext().getAttribute(
                RemoteApiSessionManager.class.getCanonicalName() + ".INSTANCE");
    }

    public Session getSession(String sid) {
        synchronized (mySessions) {
            if (mySessions.containsKey(sid)) {
                mySessions.get(sid).touch();
                if (mySessions.containsKey(sid)) {
                    return mySessions.get(sid);
                }
            }
        }
        return null;
    }

    public void addSession(String sid, Session session) {
        synchronized (mySessions) {
            mySessions.put(sid, session);
        }
    }
}