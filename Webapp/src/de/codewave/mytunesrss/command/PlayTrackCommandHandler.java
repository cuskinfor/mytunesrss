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

    @Override
    public void execute() throws SQLException, IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        FileSender fileSender;
        if (needsAuthorization()) {
            fileSender = new StatusCodeFileSender(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            String trackId = getRequest().getParameter("track");
            Collection<Track> tracks = getDataStore().executeQuery(FindTrackQuery.getForId(new String[] {trackId}));
            if (!tracks.isEmpty()) {
                Track track = tracks.iterator().next();
                File file = track.getFile();
                String contentType = track.getContentType();
                if (!file.exists()) {
                    try {
                        fileSender = new FileSender(new File(MyTunesRss.class.getResource("failure.mp3").toURI()), "audio/mp3", 1024 * 50);
                    } catch (URISyntaxException e) {
                        fileSender = new StatusCodeFileSender(HttpServletResponse.SC_NO_CONTENT);
                    }
                } else {
                    fileSender = new FileSender(file, contentType, 1024 * 50);
                }
            } else {
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