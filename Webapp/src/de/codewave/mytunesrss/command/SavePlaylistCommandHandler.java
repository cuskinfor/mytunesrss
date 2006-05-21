/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.datastore.*;

import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.SavePlaylistCommandHandler
 */
public class SavePlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        String name = getRequestParameter("name", "");
        Playlist playlist = (Playlist)getSession().getAttribute("playlist");
        Collection<Track> playlistContent = (Collection<Track>)getSession().getAttribute("playlistContent");
        if (StringUtils.isNotEmpty(name)) {
            playlist.setName(name);
            DataStoreSession session = getDataStore().getTransaction();
            session.begin();
            InsertPlaylistStatement statement = new InsertMyTunesPlaylistStatement();
            statement.setName(name);
            statement.setTrackIds(getTrackIds(playlistContent));
            session.executeStatement(statement);
            session.commit();
            getSession().removeAttribute("playlist");
            getSession().removeAttribute("playlistContent");
            forward(MyTunesRssCommand.ShowPortal);
        } else {
            setError("missing playlist name!");
            getResponse().sendRedirect(getRequestParameter("backUrl", null));
        }
    }

    private List<String> getTrackIds(Collection<Track> playlistContent) {
        List<String> trackIds = new ArrayList<String>(playlistContent.size());
        for (Track track : playlistContent) {
            trackIds.add(track.getId());
        }
        return trackIds;
    }
}