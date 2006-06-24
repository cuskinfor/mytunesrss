/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.servlet.*;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.command.PlayTrackCommandHandler
 */
public class PlayTrackCommandHandler extends MyTunesRssCommandHandler {
    private static final Log LOG = LogFactory.getLog(PlayTrackCommandHandler.class);
    private static final int BUFFER_SIZE = 1024 * 50;

    @Override
    public void execute() throws SQLException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        FileSender fileSender;
        if (needsAuthorization()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Not authorized to request track, sending response code SC_UNAUTHORIZED.");
            }
            fileSender = new StatusCodeFileSender(HttpServletResponse.SC_UNAUTHORIZED);
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
                    try {
                        fileSender = new FileSender(new File(MyTunesRss.class.getResource("failure.mp3").toURI()), "audio/mp3", BUFFER_SIZE);
                    } catch (URISyntaxException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Could not send error sound file, sending response code SC_NO_CONTENT instead.", e);
                        }
                        fileSender = new StatusCodeFileSender(HttpServletResponse.SC_NO_CONTENT);
                    }
                } else {
                    fileSender = new FileSender(file, contentType, BUFFER_SIZE);
                }
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

        public StatusCodeFileSender(int statusCode) {
            super(null, null);
            myStatusCode = statusCode;
        }

        @Override
        public void sendGetResponse(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse, boolean throwException) throws IOException {
            httpServletResponse.setStatus(myStatusCode);
        }

        @Override
        public void sendHeadResponse(HttpServletRequest servletRequest, HttpServletResponse httpServletResponse) {
            httpServletResponse.setStatus(myStatusCode);
        }
    }
}