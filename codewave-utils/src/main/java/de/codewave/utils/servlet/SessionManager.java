/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

import org.apache.commons.lang3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * A session manager handles information on all session in a web application, such as connect time, last access time, remote address, etc. For this
 * class to work correctly it must be registered as a servlet context listener to the web application, as a session listener and as a filter for all
 * relevant servlet requests.
 */
public class SessionManager implements ServletContextListener, Filter, HttpSessionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);
    private static Class<SessionInfo> theSessionInfoClass = SessionInfo.class;

    public static Collection<? extends SessionInfo> getAllSessionInfo(ServletContext servletContext) {
        SessionInfoContainer infoContainer = (SessionInfoContainer)servletContext.getAttribute(SessionManager.class.getName());
        return (Collection<? extends SessionInfo>)(infoContainer != null ? infoContainer.getAllSessionInfo() : Collections.emptySet());
    }

    public static SessionInfo getSessionInfo(HttpServletRequest servletRequest) {
        return getSessionInfo(servletRequest.getSession());
    }

    public static SessionInfo getSessionInfo(HttpSession session) {
        SessionInfoContainer infoContainer = (SessionInfoContainer)session.getServletContext().getAttribute(SessionManager.class.getName());
        return infoContainer != null ? infoContainer.getSessionInfo(session) : null;
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setAttribute(SessionManager.class.getName(), new SessionInfoContainer());
        String className = null;
        try {
            className = servletContextEvent.getServletContext().getInitParameter("sessionInfoClass");
            if (StringUtils.isNotEmpty(className)) {
                theSessionInfoClass = (Class<SessionInfo>)Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not load class for name \"" + className + "\".", e);
            }
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().removeAttribute(SessionManager.class.getName());
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        SessionInfoContainer infoContainer = (SessionInfoContainer)((HttpServletRequest)servletRequest).getSession().getServletContext().getAttribute(
                SessionManager.class.getName());
        if (infoContainer != null) {
            infoContainer.handleRequest((HttpServletRequest)servletRequest);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        // intentionally left blank
    }

    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        // intentionally left blank
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        SessionInfoContainer infoContainer =
                (SessionInfoContainer)httpSessionEvent.getSession().getServletContext().getAttribute(SessionManager.class.getName());
        if (infoContainer != null) {
            infoContainer.removeSession(httpSessionEvent.getSession());
        }
    }

    public static class SessionInfoContainer {
        private Map<String, SessionInfo> mySessionInfos = new HashMap<String, SessionInfo>();

        public void handleRequest(HttpServletRequest servletRequest) {
            SessionInfo info = mySessionInfos.get(servletRequest.getSession().getId());
            if (info == null) {
                synchronized (mySessionInfos) {
                    info = mySessionInfos.get(servletRequest.getSession().getId());
                    if (info == null) {
                        try {
                            info = theSessionInfoClass.getConstructor(HttpServletRequest.class).newInstance(servletRequest);
                        } catch (Exception e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("Could not instantiate session info class for type \"" + theSessionInfoClass.getName() +
                                        "\". Using default implementation instead.", e);
                            }
                            info = new SessionInfo(servletRequest);
                        }
                        mySessionInfos.put(servletRequest.getSession().getId(), info);
                    }
                }
            }
            info.touch();
        }

        public void removeSession(HttpSession session) {
            mySessionInfos.remove(session.getId());
        }

        public SessionInfo getSessionInfo(HttpSession session) {
            return mySessionInfos.get(session.getId());
        }

        public Collection<? extends SessionInfo> getAllSessionInfo() {
            return new HashSet<SessionInfo>(mySessionInfos.values());
        }
    }

    public static class SessionInfo {
        private String mySessionId;
        private String myRemoteAddress;
        private String myRemoteHost;
        private String myRemoteUser;
        private long myConnectTime;
        private long myLastAccessTime;
        private String myBestRemoteAddress;

        public SessionInfo(HttpServletRequest servletRequest) {
            mySessionId = servletRequest.getSession().getId();
            myRemoteAddress = servletRequest.getRemoteAddr();
            myRemoteHost = servletRequest.getRemoteHost();
            myRemoteUser = servletRequest.getRemoteUser();
            myConnectTime = System.currentTimeMillis();
            myBestRemoteAddress = ServletUtils.getBestRemoteAddress(servletRequest);
            touch();
        }

        public long getConnectTime() {
            return myConnectTime;
        }

        public long getLastAccessTime() {
            return myLastAccessTime;
        }

        public void touch() {
            myLastAccessTime = System.currentTimeMillis();
        }

        public String getSessionId() {
            return mySessionId;
        }

        public String getRemoteAddress() {
            return myRemoteAddress;
        }

        public String getRemoteHost() {
            return myRemoteHost;
        }

        public String getRemoteUser() {
            return myRemoteUser;
        }

        public String getBestRemoteAddress() {
            return myBestRemoteAddress;
        }
    }
}
