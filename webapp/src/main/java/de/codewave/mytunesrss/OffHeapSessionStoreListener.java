package de.codewave.mytunesrss;

import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.OffHeapStore;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.IOException;
import java.util.Map;

public class OffHeapSessionStoreListener implements ServletContextListener, HttpSessionListener, Filter {

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

    public void init(FilterConfig filterConfig) throws ServletException {
        // nothing to initialize
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            String currentListId = request.getParameter(OffHeapSessionStore.CURRENT_LIST_ID);
            if (StringUtils.isBlank(currentListId)) {
                OffHeapSessionStore.get((HttpServletRequest) request).removeCurrentList();
            }

        }
        chain.doFilter(request, response);
    }

    public void destroy() {
        // nothing to destroy
    }
}
