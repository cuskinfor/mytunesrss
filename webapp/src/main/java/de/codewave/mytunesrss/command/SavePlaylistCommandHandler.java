/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.SavePlaylistCommandHandler
 */
public class SavePlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isCreatePlaylists()) {
            String name = getRequestParameter("name", "");
            Playlist playlist = (Playlist)getSession().getAttribute("playlist");
            Collection<Track> playlistContent = (Collection<Track>)getSession().getAttribute("playlistContent");
            if (StringUtils.isNotEmpty(name)) {
                playlist.setName(name);
                SavePlaylistStatement statement = new SaveMyTunesPlaylistStatement(getAuthUser().getName(), getBooleanRequestParameter("user_private", false));
                statement.setId(playlist.getId());
                statement.setName(name);
                statement.setTrackIds(getTrackIds(playlistContent));
                getTransaction().executeStatement(statement);
                getSession().removeAttribute("playlist");
                getSession().removeAttribute("playlistContent");
                getStates().put("addToPlaylistMode", Boolean.FALSE);
                forward(MyTunesRssCommand.ShowPortal);
            } else {
                addError(new BundleError("error.needPlaylistNameForSave"));
                redirect(MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null)));
            }
        } else {
            forward(MyTunesRssResource.Login);
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