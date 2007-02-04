/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.CreateM3uCommandHandler
 */
public class CreatePlaylistCommandHandler extends MyTunesRssCommandHandler {
    protected Collection<Track> getTracks() throws SQLException, IOException, ServletException {
        String playlistId = getRequest().getParameter("playlist");
        String album = MyTunesRssBase64Utils.decodeToString(getRequestParameter("album", null));
        String artist = MyTunesRssBase64Utils.decodeToString(getRequestParameter("artist", null));
        String genre = MyTunesRssBase64Utils.decodeToString(getRequestParameter("genre", null));
        String[] trackIds = getNonEmptyParameterValues("track");
        String trackList = getRequestParameter("tracklist", null);
        if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
            trackIds = StringUtils.split(trackList, ',');
        }
        Collection<Track> tracks = Collections.emptyList();
        if (StringUtils.isNotEmpty(playlistId)) {
            tracks = getDataStore().executeQuery(new FindPlaylistTracksQuery(playlistId));
        } else if (trackIds != null && trackIds.length > 0) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForId(trackIds));
        } else if (StringUtils.isNotEmpty(album)) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForAlbum(new String[] {album}, false));
        } else if (StringUtils.isNotEmpty(artist)) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForArtist(new String[] {artist}, false));
        } else if (StringUtils.isNotEmpty(genre)) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForGenre(new String[] {genre}, false));
        }
        return tracks;
    }

}