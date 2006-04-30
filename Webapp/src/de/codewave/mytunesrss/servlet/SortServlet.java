/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class SortServlet extends BaseServlet {
    private static final Log LOG = LogFactory.getLog(SortServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String sortParam = request.getParameter("sortOrder");
        if (sortParam != null) {
            try {
                SortOrder sortOrder = SortOrder.valueOf(sortParam);
                createSectionsAndForward(request, response, sortOrder);
            } catch (IllegalArgumentException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Illegal sort order request.", e);
                }
                request.getRequestDispatcher("/search.jsp").forward(request, response);
            }
        } else {
            request.getRequestDispatcher("/search.jsp").forward(request, response);
        }
    }
}