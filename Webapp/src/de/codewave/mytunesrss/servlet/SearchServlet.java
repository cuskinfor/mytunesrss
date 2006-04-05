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

import org.apache.commons.logging.*;

public class SearchServlet extends BaseServlet {
    private static final Log LOG = LogFactory.getLog(SearchServlet.class);

    private static final int MAXIMUM_RESULTS = 200;

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
        removeMissingFiles(matchingFiles);
        if (matchingFiles != null && !matchingFiles.isEmpty()) {
            if (matchingFiles.size() < MAXIMUM_RESULTS) {
                request.getSession().setAttribute("searchResult", matchingFiles);
                createSectionsAndForward(request, response, SortOrder.Album);
            } else {
                request.setAttribute("error", "error.too_many_results");
                request.setAttribute("errorParam0", new Integer(MAXIMUM_RESULTS));
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("error", "error.no_matching_songs");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    private void removeMissingFiles(Collection<MusicFile> matchingFiles) {
        for (Iterator<MusicFile> filesIterator = matchingFiles.iterator(); filesIterator.hasNext(); ) {
            MusicFile file = filesIterator.next();
            if (!file.getFile().exists() || !file.getFile().isFile()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Title \"" + file.getName() + "\" with missing file \"" + file.getFile() + "\" removed from search result.");
                }
                filesIterator.remove();
            }
        }
    }
}