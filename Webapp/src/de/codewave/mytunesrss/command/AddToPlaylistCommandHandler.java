/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.servlet.*;

import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.AddToPlaylistCommandHandler
 */
public class AddToPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        Collection<Track> playlist = (Collection<Track>)getSession().getAttribute("playlistContent");
        String trackId = getRequestParameter("track", null);
        String album = getRequestParameter("album", null);
        String artist = getRequestParameter("artist", null);
        DataStoreQuery query;
        if (StringUtils.isNotEmpty(trackId)) {
            query = FindTrackQuery.getForId(trackId);
        } else if (StringUtils.isNotEmpty(album)) {
            query = FindTrackQuery.getForAlbum(album, false);
        } else {
            query = FindTrackQuery.getForArtist(artist, false);
        }
        playlist.addAll(getDataStore().executeQuery(query));
        ((Playlist)getSession().getAttribute("playlist")).setTrackCount(playlist.size());
        String backUrl = getRequestParameter("backUrl", null);
        if (StringUtils.isNotEmpty(backUrl)) {
            getResponse().sendRedirect(backUrl);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}