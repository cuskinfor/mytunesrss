/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Authentication filter invoked.");
        }
        HttpSession session = ((HttpServletRequest)servletRequest).getSession();
        MyTunesRssConfig config = (MyTunesRssConfig)session.getServletContext().getAttribute(MyTunesRssConfig.class.getName());
        if (!MyTunesRss.REGISTERED || !config.isAuth() || StringUtils.isNotEmpty((String)session.getAttribute("authHash"))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No authentication necessary.");
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Forwarding to login page.");
            }
            servletRequest.getRequestDispatcher("/login.jsp").forward(servletRequest, servletResponse);
        }
    }

    public void destroy() {
        // intentionally left blank
    }
}