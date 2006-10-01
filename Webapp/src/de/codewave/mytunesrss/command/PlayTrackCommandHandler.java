/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.logging.*;

import javax.servlet.http.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.PlayTrackCommandHandler
 */
public class PlayTrackCommandHandler extends MyTunesRssCommandHandler {
    private static final Log LOG = LogFactory.getLog(PlayTrackCommandHandler.class);
    private static final int BUFFER_SIZE = 1024 * 50;

    @Override
    public void execute() throws IOException, SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        FileSender fileSender;
        if (needsAuthorization()) {
            fileSender = new StatusCodeFileSender(HttpServletResponse.SC_NO_CONTENT);
        } else {
            String trackId = getRequest().getParameter("track");
            Collection<Track> tracks = getDataStore().executeQuery(FindTrackQuery.getForId(new String[] {trackId}));
            if (!tracks.isEmpty()) {
                Track track = tracks.iterator().next();
                File file = track.getFile();
                String contentType = track.getContentType();
                if (!file.exists()) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Requested file \"" + file.getAbsolutePath() + "\" does not exist.");
                    }
                    fileSender = new StatusCodeFileSender(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    fileSender = new FileSender(file, contentType, (int)file.length(), BUFFER_SIZE);
                }
                fileSender.setCounter((FileSender.ByteSentCounter)SessionManager.getSessionInfo(getRequest()));
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("No tracks recognized in request, sending response code SC_NO_CONTENT instead.");
                }
                fileSender = new StatusCodeFileSender(HttpServletResponse.SC_NO_CONTENT);
            }
        }
        if ("head".equalsIgnoreCase(getRequest().getMethod())) {
            fileSender.sendHeadResponse(getRequest(), getResponse());
        } else {
            fileSender.sendGetResponse(getRequest(), getResponse(), false);
        }
    }

    private static class StatusCodeFileSender extends FileSender {
        private int myStatusCode;

        public StatusCodeFileSender(int statusCode) throws MalformedURLException {
            super(null, null, -1);
            myStatusCode = statusCode;
        }

        @Override
        public void sendGetResponse(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse, boolean throwException)
                throws IOException {
            httpServletResponse.setStatus(myStatusCode);
        }

        @Override
        public void sendHeadResponse(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse) {
            httpServletResponse.setStatus(myStatusCode);
        }
    }
}