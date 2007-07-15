/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.servlet.*;
import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

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
    private static final String LAME_BINARY = "/usr/local/bin/lame"; // todo: configuration
    private static final int OUTPUT_BITRATE = 32; // todo: configuration
    private static final int OUTPUT_SAMPLE_RATE = 11025; // todo: configuration

    @Override
    public void executeAuthorized() throws IOException, SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        StreamSender streamSender;
        if (!isRequestAuthorized()) {
            streamSender = new StatusCodeSender(HttpServletResponse.SC_NO_CONTENT);
        } else {
            String trackId = getRequest().getParameter("track");
            Collection<Track> tracks = getDataStore().executeQuery(FindTrackQuery.getForId(new String[] {trackId}));
            if (!tracks.isEmpty()) {
                Track track = tracks.iterator().next();
                if (!getAuthUser().isQuotaExceeded()) {
                    File file = track.getFile();
                    String contentType = track.getContentType();
                    if (!file.exists()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Requested file \"" + file.getAbsolutePath() + "\" does not exist.");
                        }
                        streamSender = new StatusCodeSender(HttpServletResponse.SC_NO_CONTENT);
                    } else {
                        if (OUTPUT_BITRATE == 0 && OUTPUT_SAMPLE_RATE == 0) {
                            streamSender = new FileSender(file, contentType, (int)file.length());
                        } else {
                            streamSender = new StreamSender(new LameTranscoderStream(file, LAME_BINARY, OUTPUT_BITRATE, OUTPUT_SAMPLE_RATE), contentType, 0);
                        }
                    }
                    streamSender.setCounter((FileSender.ByteSentCounter)SessionManager.getSessionInfo(getRequest()));
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("User limit exceeded, sending response code SC_NO_CONTENT instead.");
                    }
                    streamSender = new StatusCodeSender(HttpServletResponse.SC_NO_CONTENT);
                }
            } else {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("No tracks recognized in request, sending response code SC_NO_CONTENT instead.");
                }
                streamSender = new StatusCodeSender(HttpServletResponse.SC_NO_CONTENT);
            }
        }
        if ("head".equalsIgnoreCase(getRequest().getMethod())) {
            streamSender.sendHeadResponse(getRequest(), getResponse());
        } else {
            streamSender.sendGetResponse(getRequest(), getResponse(), false);
        }
    }

    private static class StatusCodeSender extends StreamSender {
        private int myStatusCode;

        public StatusCodeSender(int statusCode) throws MalformedURLException {
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