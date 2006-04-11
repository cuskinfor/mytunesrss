/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.itunes.*;

/**
 * de.codewave.mytunesrss.servlet.MyTunesRssServletFilter
 */
public class PlayListFilter implements Filter {
    private static final Log LOG = LogFactory.getLog(PlayListFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (PlayListFilter.LOG.isDebugEnabled()) {
            PlayListFilter.LOG.debug("Playlist filter invoked.");
        }
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary((HttpServletRequest)servletRequest);
        servletRequest.setAttribute("playlists", library.getPlayLists());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {
        // intentionally left blank
    }
}