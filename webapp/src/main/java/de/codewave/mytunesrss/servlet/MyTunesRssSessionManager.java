/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.server.MyTunesRssSessionInfo;
import de.codewave.mytunesrss.statistics.SessionEndEvent;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.utils.servlet.SessionManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSessionEvent;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.servlet.MyTunesRssSessionManager
 */
public class MyTunesRssSessionManager extends SessionManager {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (!"true".equalsIgnoreCase(servletRequest.getParameter("ignoreSession"))) {
            super.doFilter(servletRequest, servletResponse, filterChain);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        MyTunesRssSessionInfo sessionInfo = (MyTunesRssSessionInfo) getSessionInfo(httpSessionEvent.getSession());
        User user = sessionInfo.getUser();
        if (user != null) {
            SessionEndEvent event = new SessionEndEvent(user.getName(), httpSessionEvent.getSession().getId());
            event.setEventTime(sessionInfo.getLastAccessTime());
            event.myDuration = sessionInfo.getLastAccessTime() - sessionInfo.getConnectTime();
            StatisticsEventManager.getInstance().fireEvent(event);
        }
        super.sessionDestroyed(httpSessionEvent);
    }
}