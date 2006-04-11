/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class PlayListFeedServlet extends BaseServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String playListId = request.getParameter("playlist");
        if (StringUtils.isNotEmpty(playListId)) {
            Map<String, String> urls = (Map<String, String>)request.getSession().getAttribute("urlMap");
            StringBuffer url = new StringBuffer(urls.get("rss")).append("/playlist=").append(playListId);
            response.sendRedirect(url.toString());
        } else {
            request.setAttribute("error", "error.must_select_a_playlist");
            request.getRequestDispatcher("/search.jsp").forward(request, response);
        }
    }
}