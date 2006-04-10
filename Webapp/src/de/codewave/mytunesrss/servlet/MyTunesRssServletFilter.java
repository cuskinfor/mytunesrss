/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.utils.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.servlet.MyTunesRssServletFilter
 */
public class MyTunesRssServletFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest)servletRequest).getSession();
        if (session.getAttribute("urlMap") == null) {
            Map<String, String> servletUrls = new HashMap<String, String>();
            servletUrls.put("search", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SearchServlet.class));
            servletUrls.put("sort", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SortServlet.class));
            servletUrls.put("rss", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, RSSFeedServlet.class));
            servletUrls.put("select", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SelectServlet.class));
            servletUrls.put("mp3", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, MP3DeliveryServlet.class));
            servletUrls.put("index", ServletUtils.getApplicationUrl((HttpServletRequest)servletRequest));
            session.setAttribute("urlMap", servletUrls);
        }
        servletRequest.setAttribute("useAuth", Boolean.valueOf(System.getProperty("mytunesrss.useAuth", "false")));
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        // intentionally left blank
    }
}