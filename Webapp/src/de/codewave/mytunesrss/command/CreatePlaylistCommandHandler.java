/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;

import javax.servlet.*;
import java.sql.*;
import java.io.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.CreateM3uCommandHandler
 */
public class CreatePlaylistCommandHandler extends MyTunesRssCommandHandler {

    protected void createDataAndForward(MyTunesRssResource playlistResource) throws SQLException, IOException, ServletException {
        String playlistId = getRequest().getParameter("playlist");
        String[] albums = getNonEmptyParameterValues("album");
        String[] artists = getNonEmptyParameterValues("artist");
        String[] trackIds = getNonEmptyParameterValues("track");
        String trackList = getRequestParameter("tracklist", null);
        if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
            trackIds = StringUtils.split(trackList, ',');
        }
        Collection<Track> tracks;
        if (StringUtils.isNotEmpty(playlistId)) {
            tracks = getDataStore().executeQuery(new FindPlaylistTracksQuery(playlistId));
        } else if (trackIds != null && trackIds.length > 0) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForId(trackIds));
        } else if (albums != null && albums.length > 0) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForAlbum(albums, false));
        } else if (artists != null && artists.length > 0) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForArtist(artists, false));
        } else {
            tracks = Collections.emptyList();
        }
        if (!tracks.isEmpty()) {
            getRequest().setAttribute("tracks", tracks);
            forward(playlistResource);
        } else {
            setError("error.emptyFeed");
            forward(MyTunesRssCommand.ShowPortal); // todo: redirect to backUrl
        }
    }

}