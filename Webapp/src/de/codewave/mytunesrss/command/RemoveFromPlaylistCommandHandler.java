/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.RemoveFromPlaylistCommandHandler
 */
public class RemoveFromPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        Collection<Track> playlistContent = (Collection<Track>)getSession().getAttribute("playlistContent");
        String[] trackIds = getNonEmptyParameterValues("track");
        if (trackIds != null && trackIds.length > 0) {
            Track dummyTrack = new Track();
            for (String trackId : trackIds) {
                dummyTrack.setId(trackId);
                playlistContent.remove(dummyTrack);
            }
            Playlist playlist = (Playlist)getSession().getAttribute("playlist");
            playlist.setTrackCount(playlistContent.size());
        } else {
            setError("@@must select at least one track");
        }
        forward(MyTunesRssCommand.EditPlaylist);
    }
}