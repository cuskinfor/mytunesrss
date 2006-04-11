/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.itunes.*;
import de.codewave.mytunesrss.musicfile.*;
import de.codewave.utils.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.logging.*;

public class MP3DeliveryServlet extends HttpServlet {
    private static final Log LOG = LogFactory.getLog(MP3DeliveryServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FileSender sender = getFileSender(request);
        if (sender != null) {
            sender.sendGetResponse(request, response);
        } else {
            returnError(request, response);
        }
    }

    private void returnError(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FileSender sender = getFileSender(request);
        if (sender != null) {
            sender.sendHeadResponse(request, response);
        } else {
            returnError(request, response);
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
                if (title != null && title.getFile().exists() && title.getFile().isFile()) {
                    return new FileSender(title.getFile(), title.getContentType());
                } else {
                    if (LOG.isErrorEnabled()) {
                        if (title == null) {
                            LOG.error("Title with ID \"" + titleId + "\" not found in iTunes library.");
                        } else {
                            LOG.error("File not found for delivery: \"" + title.getFile() + "\".");
                        }
                    }
                }
            }
        }
        return null;
    }
}
