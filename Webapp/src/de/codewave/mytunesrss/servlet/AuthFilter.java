/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.itunes.*;
import de.codewave.utils.servlet.*;

/**
 * de.codewave.mytunesrss.servlet.MyTunesRssServletFilter
 */
public class AuthFilter implements Filter {
    private static final Log LOG = LogFactory.getLog(AuthFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (needsAuth(servletRequest)) {
            if (AuthFilter.LOG.isDebugEnabled()) {
                AuthFilter.LOG.debug("Forwarding to login page.");
            }
            servletRequest.getRequestDispatcher("/login.jsp").forward(servletRequest, servletResponse);
        } else {
            if (AuthFilter.LOG.isDebugEnabled()) {
                AuthFilter.LOG.debug("Executing filter chain.");
            }
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean needsAuth(ServletRequest servletRequest) {
        HttpSession session = ((HttpServletRequest)servletRequest).getSession();
        MyTunesRssConfig config = (MyTunesRssConfig)session.getServletContext().getAttribute(MyTunesRssConfig.class.getName());
        if (!MyTunesRss.REGISTERED || !config.isAuth() || StringUtils.isNotEmpty((String)session.getAttribute("authHash"))) {
            if (AuthFilter.LOG.isDebugEnabled()) {
                AuthFilter.LOG.debug("No authentication necessary.");
            }
            return false;
        } else {
            if (AuthFilter.LOG.isDebugEnabled()) {
                AuthFilter.LOG.debug("Needs authentication.");
            }
            return true;
        }
    }

    public void destroy() {
        // intentionally left blank
    }
}