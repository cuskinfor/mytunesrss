/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;

import de.codewave.mytunesrss.*;

public class LoginServlet extends BaseServlet {
    private static final Log LOG = LogFactory.getLog(LoginServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String password = request.getParameter("password");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Trying to authenticate user with password.");
        }
        MyTunesRssConfig config = getMyTunesRssConfig(request);
        if (password.hashCode() == config.getPasswordHash()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Login successful.");
            }
            request.getSession().setAttribute("authHash", Integer.toString(config.getPasswordHash()));
            request.getRequestDispatcher("/search.jsp").forward(request, response);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Login denied.");
            }
            request.setAttribute("error", "error.login_denied");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

}