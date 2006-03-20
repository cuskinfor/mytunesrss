/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.rss4psp.servlet;

import de.codewave.utils.servlet.*;
import de.codewave.rss4psp.musicfile.*;
import de.codewave.rss4psp.itunes.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class MP3DeliveryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FileSender sender = getFileSender(request);
        if (sender != null) {
            sender.sendGetResponse(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FileSender sender = getFileSender(request);
        if (sender != null) {
            sender.sendHeadResponse(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    private FileSender getFileSender(HttpServletRequest request) {
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
        String pathInfo = request.getPathInfo();
        if (pathInfo.length() > 1) {
            int slash = pathInfo.indexOf('/', 1);
            if (slash > -1) {
                String titleId = pathInfo.substring(1, slash);
                List<MusicFile> matchingFiles = library.getMatchingFiles(new MusicFileIdSearch(titleId));
                MusicFile title = matchingFiles != null && !matchingFiles.isEmpty() ? matchingFiles.get(0) : null;
                if (title != null) {
                    return new FileSender(title.getFile(), "audio/mp3");
                }
            }
        }
        return null;
    }
}
