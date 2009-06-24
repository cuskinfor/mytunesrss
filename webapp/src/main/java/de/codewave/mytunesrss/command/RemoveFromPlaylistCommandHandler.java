/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.service.EditPlaylistService;

import java.util.Collection;

/**
 * de.codewave.mytunesrss.command.RemoveFromPlaylistCommandHandler
 */
public class RemoveFromPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            Collection<Track> playlistContent = (Collection<Track>) MyTunesRssRemoteEnv.getSessionForRegularSession(getRequest()).getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS);
            String[] trackIds = getNonEmptyParameterValues("track");
            if (trackIds != null && trackIds.length > 0) {
                Track dummyTrack = new Track();
                for (String trackId : trackIds) {
                    dummyTrack.setId(trackId);
                    playlistContent.remove(dummyTrack);
                }
                Playlist playlist = (Playlist) MyTunesRssRemoteEnv.getSessionForRegularSession(getRequest()).getAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST);
                playlist.setTrackCount(playlistContent.size());
            } else {
                addError(new BundleError("error.deleteFromPlaylistNoTrack"));
            }
            forward(MyTunesRssCommand.EditPlaylist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}