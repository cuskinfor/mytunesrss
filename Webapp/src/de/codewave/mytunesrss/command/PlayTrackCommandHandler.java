/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.servlet.*;

import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * de.codewave.mytunesrss.command.PlayTrackCommandHandler
 */
public class PlayTrackCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void execute() throws Exception {
        FileSender fileSender;
        if (needsAuthorization()) {
            fileSender = new StatusCodeFileSender(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            String trackId = getRequest().getParameter("track");
            Collection<Track> tracks = getDataStore().executeQuery(new FindTrackQuery(trackId));
            if (!tracks.isEmpty()) {
                Track track = tracks.iterator().next();
                fileSender = new FileSender(track.getFile(), getContentType(track));
            } else {
                fileSender = new StatusCodeFileSender(HttpServletResponse.SC_NO_CONTENT);
            }
        }
        if ("head".equalsIgnoreCase(getRequest().getMethod())) {
            fileSender.sendHeadResponse(getRequest(), getResponse());
        } else {
            fileSender.sendGetResponse(getRequest(), getResponse());
        }
    }

    private String getContentType(Track track) {
        String name = track.getFile().getName().toLowerCase();
        if (name.endsWith(".mp3")) {
            return "audio/mp3";
        } else if (name.endsWith(".m4p") || name.endsWith(".m4a") || name.endsWith(".mp4")) {
            return "audio/mp4";
        }
        return URLConnection.guessContentTypeFromName(name);
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