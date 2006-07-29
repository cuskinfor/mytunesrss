/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.mp3.*;
import de.codewave.utils.*;

import javax.servlet.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.lang.Error;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.CreateM3uCommandHandler
 */
public class CreatePlaylistCommandHandler extends MyTunesRssCommandHandler {

    protected void createDataAndForward(MyTunesRssResource playlistResource) throws SQLException, IOException, ServletException {
        String playlistId = getRequest().getParameter("playlist");
        String album = getRequestParameter("album", null);
        String artist = getRequestParameter("artist", null);
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
        } else if (StringUtils.isNotEmpty(album)) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForAlbum(new String[] {MiscUtils.getStringFromHexString(album)}, false));
        } else if (StringUtils.isNotEmpty(artist)) {
            tracks = getDataStore().executeQuery(FindTrackQuery.getForArtist(new String[] {MiscUtils.getStringFromHexString(artist)}, false));
        } else {
            tracks = Collections.emptyList();
        }
        if (!tracks.isEmpty()) {
            getRequest().setAttribute("tracks", tracks);
            for (Track track : tracks) {
                Image image = ID3Utils.getImage(track);
                if (image != null) {
                    getRequest().setAttribute("imageTrackId", track.getId());
                    break; // use first available image
                }
            }
            forward(playlistResource);
        } else {
            addError(new BundleError("error.emptyFeed"));
            forward(MyTunesRssCommand.ShowPortal); // todo: redirect to backUrl
        }
    }

}