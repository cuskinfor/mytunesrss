/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.rss4psp.itunes;

import org.apache.commons.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

/**
 * de.codewave.rss4psp.itunes.ITunesLibraryContextListener
 */
public class ITunesLibraryContextListener implements ServletContextListener {
    private static final Log LOG = LogFactory.getLog(ITunesLibraryContextListener.class);
    private static final String LIBRARY_KEY = "iTunesLibrary";

    public static ITunesLibrary getLibrary(HttpServletRequest request) {
        return (ITunesLibrary)request.getSession().getServletContext().getAttribute(LIBRARY_KEY);
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ITunesLibrary library = new ITunesLibrary();
        try {
            library.load(new File(servletContextEvent.getServletContext().getInitParameter("iTunesLibrary")).toURL());
        } catch (MalformedURLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load iTunes library xml.", e);
            }
            library = new ITunesLibrary(); // empty library if any error occured
        }
        servletContextEvent.getServletContext().setAttribute(LIBRARY_KEY, library);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        // intentionally left blank
    }
}