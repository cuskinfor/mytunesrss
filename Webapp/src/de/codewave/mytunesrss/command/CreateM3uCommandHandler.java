/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.CreateM3uCommandHandler
 */
public class CreateM3uCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        String playlistId = getRequest().getParameter("playlist");
        String album = getRequest().getParameter("album");
        String artist = getRequest().getParameter("artist");
        Collection<Track> tracks;
        if (StringUtils.isNotEmpty(playlistId)) {
            tracks = getDataStore().executeQuery(new FindPlaylistTracksQuery(playlistId));
        } else if (StringUtils.isNotEmpty(album)) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForAlbum(album));
        } else {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForArtist(artist));
        }
        if (!tracks.isEmpty()) {
            getRequest().setAttribute("tracks", tracks);
            getRequest().setAttribute("authHash", getAuthHash());
            forward(MyTunesRssResource.TemplateM3u);
        } else {
            setError("error.emptyFeed");
            forward(MyTunesRssResource.Portal);
        }
    }

}