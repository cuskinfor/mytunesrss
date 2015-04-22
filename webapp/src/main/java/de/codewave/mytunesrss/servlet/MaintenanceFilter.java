package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventListener;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MaintenanceFilter implements Filter, MyTunesRssEventListener {

    private volatile boolean myMaintenanceMode;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (myMaintenanceMode) {
            ((HttpServletResponse)servletResponse).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            servletRequest.getRequestDispatcher("/maintenance_mode.jsp").forward(servletRequest, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        MyTunesRssEventManager.getInstance().removeListener(this);
    }

    @Override
    public void handleEvent(MyTunesRssEvent event) {
        switch (event.getType()) {
            case MAINTENANCE_START:
                myMaintenanceMode = true;
                break;
            case MAINTENANCE_STOP:
                myMaintenanceMode = false;
                break;
            default:
                // ignore all other events
        }
    }
}
