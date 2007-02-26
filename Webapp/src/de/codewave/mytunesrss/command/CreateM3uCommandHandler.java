/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.CreateM3uCommandHandler
 */
public class CreateM3uCommandHandler extends CreatePlaylistCommandHandler {

    @Override
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        if (getAuthUser().isPlaylist()) {
            Collection<Track> tracks = getTracks();
            if (tracks != null && !tracks.isEmpty()) {
                getRequest().setAttribute("tracks", tracks);
                forward(MyTunesRssResource.TemplateM3u);
            } else {
                addError(new BundleError("error.emptyFeed"));
                forward(MyTunesRssCommand.ShowPortal);// todo: redirect to backUrl
            }
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

}