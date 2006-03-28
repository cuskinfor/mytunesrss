/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.itunes.*;
import de.codewave.mytunesrss.musicfile.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class SelectServlet extends BaseServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Collection<String> requestSelection = getRequestSelection(request);
        if (!requestSelection.isEmpty()) {
            ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
            List<MusicFile> selectedFiles = new ArrayList<MusicFile>();
            for (String id : requestSelection) {
                selectedFiles.addAll(library.getMatchingFiles(new MusicFileIdSearch(id)));
            }
            Collection<MusicFile> playlist = (Collection<MusicFile>)request.getSession().getAttribute("playlist");
            if (playlist == null) {
                playlist = new ArrayList<MusicFile>();
                request.getSession().setAttribute("playlist", playlist);
            }
            playlist.addAll(selectedFiles);
            if ("true".equalsIgnoreCase(request.getParameter("final"))) {
                String channel = request.getParameter("channel");
                if (StringUtils.isEmpty(channel)) {
                    channel = "Codewave MyTunesRSS v" + System.getProperty("mytunesrss.version");
                }
                StringBuffer url = new StringBuffer(ServletUtils.getApplicationUrl(request)).append("/rss/channel=").append(channel);
                for (MusicFile musicFile : playlist) {
                    url.append("/").append(musicFile.getId());
                }
                response.sendRedirect(url.toString());
            } else {
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            }
        } else {
            SortOrder sortOrder = SortOrder.valueOf(request.getParameter("sortOrder"));
            createSectionsAndForward(request, response, sortOrder);
        }
    }
}