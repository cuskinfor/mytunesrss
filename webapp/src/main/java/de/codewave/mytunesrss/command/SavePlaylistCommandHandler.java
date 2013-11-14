/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.SaveMyTunesPlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.rest.resource.EditPlaylistResource;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.SavePlaylistCommandHandler
 */
public class SavePlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isCreatePlaylists()) {
            String name = getRequestParameter("name", "");
            Playlist playlist = (Playlist) getRequest().getSession().getAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST);
            Collection<Track> playlistContent = (Collection<Track>) getRequest().getSession().getAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST_TRACKS);
            if (StringUtils.isNotEmpty(name)) {
                playlist.setName(name);
                SavePlaylistStatement statement = new SaveMyTunesPlaylistStatement(getAuthUser().getName(), getBooleanRequestParameter("user_private",
                                                                                                                                       false));
                if (!getAuthUser().isCreatePublicPlaylists()) {
                    statement.setUserPrivate(true);
                }
                statement.setId(playlist.getId());
                statement.setName(name);
                statement.setTrackIds(getTrackIds(playlistContent));
                getTransaction().executeStatement(statement);
                getRequest().getSession().removeAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST);
                getRequest().getSession().removeAttribute(EditPlaylistResource.KEY_EDIT_PLAYLIST_TRACKS);
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
