package de.codewave.mytunesrss;

import org.h2.mvstore.MVStore;
import org.h2.mvstore.OffHeapStore;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;

public class OffHeapSessionStoreListener implements ServletContextListener, HttpSessionListener {

    static OffHeapSessionStore getOffHeapSessionStore(HttpServletRequest request) {
        return (OffHeapSessionStore) getSessions(request.getSession()).get(request.getSession().getId());
    }

    private static Map<Object, Object> getSessions(HttpSession session) {
        return getMVStore(session.getServletContext()).openMap("sessions");
    }
    
    private static MVStore getMVStore(ServletContext sc) {
        return (MVStore) sc.getAttribute(OffHeapSessionStoreListener.class.getName());
    }

    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(OffHeapSessionStoreListener.class.getName(), new MVStore.Builder().fileStore(new OffHeapStore()).open());
    }

    public void contextDestroyed(ServletContextEvent sce) {
        getMVStore(sce.getServletContext()).close();
        sce.getServletContext().removeAttribute(OffHeapSessionStoreListener.class.getName());
    }

    public void sessionCreated(HttpSessionEvent se) {
        getSessions(se.getSession()).put(se.getSession().getId(), new OffHeapSessionStore());
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        getSessions(se.getSession()).remove(se.getSession().getId());
    }
}
