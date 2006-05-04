/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.itunes.*;
import de.codewave.mytunesrss.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

import org.apache.commons.logging.*;

public class CheckHealthServlet extends HttpServlet {
    private static final Log LOG = LogFactory.getLog(CheckHealthServlet.class);

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Health check servlet called.");
        }
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
        if (library == null || library.isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Library is NULL or empty!");
            }
            response.getOutputStream().write(CheckHealthResult.EMPTY_LIBRARY);
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("Library is up and running.");
            }
            response.getOutputStream().write(CheckHealthResult.OK);
        }
    }
}
