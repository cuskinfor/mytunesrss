/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.itunes.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.servlet.MyTunesRssServletFilter
 */
public class MyTunesRssServletFilter implements Filter {
    private static final Log LOG = LogFactory.getLog(MyTunesRssServletFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        servletRequest.setCharacterEncoding("UTF-8");
        ensureUrlMapInSession(servletRequest);
        createPlayListsInRequest(servletRequest);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void createPlayListsInRequest(ServletRequest servletRequest) {
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary((HttpServletRequest)servletRequest);
        servletRequest.setAttribute("playlists", library.getPlayLists());
    }

    private void ensureUrlMapInSession(ServletRequest servletRequest) {
        HttpSession session = ((HttpServletRequest)servletRequest).getSession();
        if (session.getAttribute("urlMap") == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating servlet URL map in session.");
            }
            Map<String, String> servletUrls = new HashMap<String, String>();
            servletUrls.put("search", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SearchServlet.class));
            servletUrls.put("sort", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SortServlet.class));
            servletUrls.put("rss", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, RSSFeedServlet.class));
            servletUrls.put("select", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, SelectServlet.class));
            servletUrls.put("login", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, LoginServlet.class));
            servletUrls.put("playlist", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, PlayListFeedServlet.class));
            servletUrls.put("mp3", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, MusicDeliveryServlet.class));
            servletUrls.put("newSearch", ServletUtils.getServletUrl((HttpServletRequest)servletRequest, NewSearchServlet.class));
            session.setAttribute("urlMap", servletUrls);
        }
    }

    public void destroy() {
        // intentionally left blank
    }
}