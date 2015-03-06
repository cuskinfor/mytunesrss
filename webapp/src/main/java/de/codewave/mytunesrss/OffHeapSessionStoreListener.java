package de.codewave.mytunesrss;

import de.codewave.mytunesrss.mediarenderercontrol.MediaRendererController;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Map;

public class OffHeapSessionStoreListener implements ServletContextListener, HttpSessionListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OffHeapSessionStoreListener.class);

    static OffHeapSessionStore getOffHeapSessionStore(HttpServletRequest request) {
        return (OffHeapSessionStore) getSessions(request.getSession()).get(request.getSession().getId());
    }

    private static Map<Object, Object> getSessions(HttpSession session) {
        return MyTunesRssUtils.openMvMap(getMVStore(session.getServletContext()), "sessions");
    }

    private static MVStore getMVStore(ServletContext sc) {
        return (MVStore) sc.getAttribute(OffHeapSessionStoreListener.class.getName());
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute(OffHeapSessionStoreListener.class.getName(), MyTunesRssUtils.getMvStoreBuilder("sessions").open());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MVStore mvStore = getMVStore(sce.getServletContext());
        if (mvStore != null) {
            mvStore.close();
        }
        sce.getServletContext().removeAttribute(OffHeapSessionStoreListener.class.getName());
        // TODO: wrong class
        LOGGER.info("Removing media renderer from media renderer controller on application context destruction.");
        MediaRendererController.getInstance().setMediaRenderer(null);
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        getSessions(se.getSession()).put(se.getSession().getId(), new OffHeapSessionStore());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        getSessions(se.getSession()).remove(se.getSession().getId());
    }

}
