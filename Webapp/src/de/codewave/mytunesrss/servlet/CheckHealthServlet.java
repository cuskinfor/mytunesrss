/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.itunes.*;
import de.codewave.mytunesrss.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

public class CheckHealthServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
        if (library == null || library.isEmpty()) {
            response.getOutputStream().write(CheckHealthResult.EMPTY_LIBRARY);
        } else {
            response.getOutputStream().write(CheckHealthResult.OK);
        }
    }
}
