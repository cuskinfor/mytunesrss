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
        String[] albums = getNonEmptyParameterValues("album");
        String[] artists = getNonEmptyParameterValues("artist");
        String[] trackIds = getNonEmptyParameterValues("track");
        Collection<Track> tracks;
        if (StringUtils.isNotEmpty(playlistId)) {
            tracks = getDataStore().executeQuery(new FindPlaylistTracksQuery(playlistId));
        } else if (trackIds != null & trackIds.length > 0) {
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
            getRequest().setAttribute("authHash", getAuthHash());
            forward(MyTunesRssResource.TemplateM3u);
        } else {
            setError("error.emptyFeed");
            forward(MyTunesRssResource.Portal); // todo: redirect to backUrl
        }
    }

}