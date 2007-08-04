/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
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
                        // todo
                        // --- begin: put into method which gets a transcoder object ---
                        boolean useLame = getWebConfig().isLame();
                        int lameTargetBitrate = getWebConfig().getLameTargetBitrate();
                        int lameTargetSampleRate = getWebConfig().getLameTargetSampleRate();
                        if (StringUtils.isNotEmpty(getRequest().getParameter("lame"))) {
                            String[] splitted = getRequest().getParameter("lame").split(",");
                            if (splitted.length == 2) {
                                useLame = true;
                                lameTargetBitrate = Integer.parseInt(splitted[0]);
                                lameTargetSampleRate = Integer.parseInt(splitted[1]);
                            }
                        }
                        // --- end: put into method which gets a transcoder object ---
                        if (MyTunesRss.REGISTRATION.isRegistered() && useLame && lameTargetBitrate > 0 && lameTargetSampleRate > 0 &&
                                MyTunesRss.CONFIG.isValidLameBinary()) {
                            streamSender = new StreamSender(new LameTranscoderStream(file,
                                                                                     MyTunesRss.CONFIG.getLameBinary(),
                                                                                     lameTargetBitrate,
                                                                                     getWebConfig().getLameTargetSampleRate()), contentType, 0);
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