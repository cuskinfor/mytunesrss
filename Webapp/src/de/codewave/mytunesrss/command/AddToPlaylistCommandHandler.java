/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import org.apache.commons.lang.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.AddToPlaylistCommandHandler
 */
public class AddToPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        Collection<Track> playlist = (Collection<Track>)getSession().getAttribute("playlistContent");
        String[] trackIds = getNonEmptyParameterValues("track");
        String trackList = getRequestParameter("tracklist", null);
        if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
            trackIds = StringUtils.split(trackList, ',');
        }
        String[] albums = getNonEmptyParameterValues("album");
        String[] artists = getNonEmptyParameterValues("artist");
        DataStoreQuery<Collection<Track>> query = null;
        if (trackIds != null && trackIds.length > 0) {
            query = FindTrackQuery.getForId(trackIds);
        } else if (albums != null && albums.length > 0) {
            query = FindTrackQuery.getForAlbum(albums, false);
        } else if (artists != null && artists.length > 0) {
            query = FindTrackQuery.getForArtist(artists, false);
        }
        if (query != null) {
            playlist.addAll(getDataStore().executeQuery(query));
            ((Playlist)getSession().getAttribute("playlist")).setTrackCount(playlist.size());
        }
        String backUrl = getRequestParameter("backUrl", null);
        if (StringUtils.isNotEmpty(backUrl)) {
            redirect(backUrl);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}