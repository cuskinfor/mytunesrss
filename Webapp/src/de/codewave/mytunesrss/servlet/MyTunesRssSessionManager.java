/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.utils.servlet.*;

import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.servlet.MyTunesRssSessionManager
 */
public class MyTunesRssSessionManager extends SessionManager {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException,
            ServletException {
        if (!"true".equalsIgnoreCase(servletRequest.getParameter("ignoreSession"))) {
            super.doFilter(servletRequest, servletResponse, filterChain);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}