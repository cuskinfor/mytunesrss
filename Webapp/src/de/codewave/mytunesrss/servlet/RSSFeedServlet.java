/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.itunes.*;
import de.codewave.mytunesrss.musicfile.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class RSSFeedServlet extends BaseServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doCommand(request, response);
    }

    private void doCommand(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
        if (request.getPathInfo() != null) {
            Collection<MusicFile> feedFiles = new ArrayList<MusicFile>();
            String authHash = null;
            for (StringTokenizer tokenizer = new StringTokenizer(request.getPathInfo(), "/"); tokenizer.hasMoreTokens();) {
                String token = tokenizer.nextToken();
                if (token.startsWith("ch=")) {
                    request.setAttribute("channel", token.substring("ch=".length()));
                } else if (token.startsWith("pl=")) {
                    PlayList playlist = library.getPlayListWithId(token.substring("pl=".length()));
                    request.setAttribute("channel", playlist.getName());
                    feedFiles.addAll(playlist.getMusicFiles());
                } else if (token.startsWith("au=")) {
                    authHash = token.substring("au=".length());
                } else if (token.startsWith("id=")) {
                    feedFiles.addAll(library.getMatchingFiles(new MusicFileIdSearch(token.substring("id=".length()))));
                }
            }
            request.setAttribute("musicFiles", feedFiles);
            request.setAttribute("pubDate", new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US).format(new Date()));
            if (StringUtils.isNotEmpty(authHash)) {
                request.setAttribute("authInfo", "/au=" + authHash);
            }
            request.setAttribute("feedUrl", request.getRequestURL().toString());
            if (isAuthorized(request, authHash)) {
                request.getRequestDispatcher("/rss.jsp").forward(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            request.getRequestDispatcher("/search.jsp").forward(request, response);
        }
    }
}
