/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import org.apache.commons.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import de.codewave.mytunesrss.itunes.*;

public class PlayListFeedServlet extends BaseServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String playListId = request.getParameter("playlist");
        if (playListId != null) {
            if (playListId.length() > 0) {
                Map<String, String> urls = (Map<String, String>)request.getSession().getAttribute("urlMap");
                String feedType = request.getParameter("feedType");
                StringBuffer url = new StringBuffer(urls.get(feedType)).append("/pl=").append(playListId);
                if (StringUtils.isNotEmpty((String)request.getSession().getAttribute("authHash"))) {
                    url.append("/au=").append(request.getSession().getAttribute("authHash"));
                }
                PlayList playlist = ITunesLibraryContextListener.getLibrary(request).getPlayListWithId(playListId);
                String filename = "m3u".equals(feedType) ? "/" + playlist.getName() + ".m3u" : "";
                response.sendRedirect(url.toString() + filename);
            } else {
                request.setAttribute("error", "error.must_select_a_playlist");
                request.getRequestDispatcher("/search.jsp").forward(request, response);
            }
        } else {
            request.getRequestDispatcher("/search.jsp").forward(request, response);
        }
    }
}