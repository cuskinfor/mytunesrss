/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.CreatePlaylistCommandHandler
 */
public class CreatePlaylistCommandHandler extends CreatePlaylistBaseCommandHandler {

    @Override
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        if (getAuthUser().isPlaylist()) {
            Collection<Track> tracks = getTracks();
            if (tracks != null && !tracks.isEmpty()) {
                getRequest().setAttribute("tracks", tracks);
                String playlistType = getRequestParameter("type", null);
                if (StringUtils.isEmpty(playlistType)) {
                    forward(getWebConfig().getPlaylistTemplateResource());
                } else {
                    forward(WebConfig.PlaylistType.valueOf(playlistType).getTemplateResource());
                }
            } else {
                addError(new BundleError("error.emptyFeed"));
                forward(MyTunesRssCommand.ShowPortal);// todo: redirect to backUrl
            }
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

}