/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.servlet.*;
import de.codewave.utils.io.*;
import de.codewave.camel.mp3.*;
import de.codewave.camel.mp3.exception.*;
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

    @Override
    public void executeAuthorized() throws IOException, SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(ServletUtils.getRequestInfo(getRequest()));
        }
        StreamSender streamSender;
        Track track = null;
        if (!isRequestAuthorized()) {
            streamSender = new StatusCodeSender(HttpServletResponse.SC_NO_CONTENT);
        } else {
            String trackId = getRequest().getParameter("track");
            Collection<Track> tracks = getDataStore().executeQuery(FindTrackQuery.getForId(new String[] {trackId}));
            if (!tracks.isEmpty()) {
                track = tracks.iterator().next();
                if (!getAuthUser().isQuotaExceeded()) {
                    File file = track.getFile();
                    String contentType = track.getContentType();
                    if (!file.exists()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Requested file \"" + file.getAbsolutePath() + "\" does not exist.");
                        }
                        streamSender = new StatusCodeSender(HttpServletResponse.SC_NO_CONTENT);
                    } else {
                        Transcoder transcoder = "false".equals(getRequestParameter("notranscode", "false")) ? Transcoder.createTranscoder(track, getWebConfig(), getRequest()) : null;
                        if (transcoder != null) {
                            streamSender = transcoder.getStreamSender();
                        } else {
                            streamSender = new FileSender(file, contentType, (int)file.length());
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
        if (ServletUtils.isHeadRequest(getRequest())) {
            streamSender.sendHeadResponse(getRequest(), getResponse());
        } else {
            int bitrate = 0;
            int fileSize = 0;
            int dataOffset = 0;
            if (track != null) {
                bitrate = Mp3Utils.getMp3Info(new FileInputStream(track.getFile())).getAvgBitrate();
                fileSize = (int)track.getFile().length();
                Id3Tag tag = null;
                try {
                    tag = Mp3Utils.readId3Tag(track.getFile());
                    if (tag.isId3v2()) {
                        dataOffset = ((Id3v2Tag)tag).getHeader().getBodySize();
                    }
                } catch (IllegalHeaderException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Could not read ID3 information.", e);
                    }
                }
            }
            streamSender.setOutputStreamWrapper(getAuthUser().getOutputStreamWrapper((int)(bitrate * 1.02), dataOffset, new RangeHeader(getRequest(), fileSize)));
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