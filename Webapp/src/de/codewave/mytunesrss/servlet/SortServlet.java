/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class SortServlet extends BaseServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        SortOrder sortOrder = SortOrder.valueOf(request.getParameter("sortOrder"));
        createSectionsAndForward(request, response, sortOrder);
    }
}