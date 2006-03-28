/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.itunes.*;
import de.codewave.mytunesrss.musicfile.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class SearchServlet extends BaseServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String albumPattern = request.getParameter("album");
        String artistPattern = request.getParameter("artist");
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
        List<MusicFile> matchingFiles = library.getMatchingFiles(new MusicFileAlbumSearch(albumPattern), new MusicFileArtistSearch(artistPattern));
        if (matchingFiles != null && !matchingFiles.isEmpty()) {
            Collections.sort(matchingFiles, new AlbumComparator());
            request.getSession().setAttribute("searchResult", matchingFiles);
            createSectionsAndForward(request, response, SortOrder.Album);
        } else {
            request.setAttribute("error", "No matching songs found!");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}