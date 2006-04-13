/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.itunes.*;
import de.codewave.mytunesrss.musicfile.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

public class MusicDeliveryServlet extends BaseServlet {
    private static final Log LOG = LogFactory.getLog(MusicDeliveryServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FileSender sender = getFileSender(request);
        sender.sendGetResponse(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FileSender sender = getFileSender(request);
        sender.sendHeadResponse(request, response);
    }

    private FileSender getFileSender(HttpServletRequest request) {
        ITunesLibrary library = ITunesLibraryContextListener.getLibrary(request);
        String authHash = null;
        MusicFile title = null;
        for (StringTokenizer tokenizer = new StringTokenizer(request.getPathInfo(), "/"); tokenizer.hasMoreTokens();) {
            String token = tokenizer.nextToken();
            if (token.startsWith("ch=")) {
                request.setAttribute("channel", token.substring("ch=".length()));
            } else if (token.startsWith("id=")) {
                String titleId = token.substring("id=".length());
                List<MusicFile> titles = library.getMatchingFiles(new MusicFileIdSearch(titleId));
                if (titles != null && titles.size() == 1) {
                    title = titles.get(0);
                } else {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Title with ID \"" + titleId + "\" not found in iTunes library.");
                    }
                }
            } else if (token.startsWith("au=")) {
                authHash = token.substring("au=".length());
            }
        }
        if (title != null) {
            if (title.getFile().exists() && title.getFile().isFile()) {
                if (isAuthorized(request, authHash)) {
                    return new FileSender(title.getFile(), title.getContentType());
                }
                return new StatusCodeFileSender(HttpServletResponse.SC_UNAUTHORIZED);
            }
            if (LOG.isErrorEnabled()) {
                LOG.error("File not found for delivery: \"" + title.getFile() + "\".");
            }
        } else if (LOG.isErrorEnabled()) {
            LOG.error("No title ID specified in servlet request.");
        }
        return new StatusCodeFileSender(HttpServletResponse.SC_NO_CONTENT);
    }

    private static class StatusCodeFileSender extends FileSender {
        private int myStatusCode;

        public StatusCodeFileSender(int statusCode) {
            super(null, null);
            myStatusCode = statusCode;
        }

        @Override
        public void sendGetResponse(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse) throws IOException {
            httpServletResponse.setStatus(myStatusCode);
        }

        @Override
        public void sendHeadResponse(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse) {
            httpServletResponse.setStatus(myStatusCode);
        }
    }
}
