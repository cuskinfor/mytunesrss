/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.utils.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.servlet.AppCtxFilter
 */
public class AppCtxFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        servletRequest.setAttribute("appctx", ServletUtils.getApplicationUrl((HttpServletRequest)servletRequest));
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        // intentionally left blank
    }
}