/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.servlet.NewSearchServlet
 */
public class NewSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doAction(servletRequest, httpServletResponse);
    }


    @Override
    protected void doPost(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doAction(servletRequest, httpServletResponse);
    }

    private void doAction(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        httpServletRequest.getSession().removeAttribute("playlist");
        httpServletRequest.getRequestDispatcher("/search.jsp").forward(httpServletRequest, httpServletResponse);
    }
}