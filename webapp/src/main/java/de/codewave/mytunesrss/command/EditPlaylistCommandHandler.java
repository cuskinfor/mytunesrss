/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.EditPlaylistCommandHandler
 */
public class EditPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            Collection<Track> playlist = (Collection<Track>)getSession().getAttribute("playlistContent");
            if ((playlist != null && !playlist.isEmpty()) || getBooleanRequestParameter("allowEditEmpty", false)) {
                playlist = filterTracks(playlist);
                int pageSize = getWebConfig().getEffectivePageSize();
                if (pageSize > 0 && playlist.size() > pageSize) {
                    int index = getValidIndex(getSafeIntegerRequestParameter("index", 0), pageSize, playlist.size());
                    getRequest().setAttribute("tracks", new ArrayList<Track>(playlist).subList(index * pageSize, Math.min((index * pageSize) + pageSize,
                                                                                                                          playlist.size())));
                    getRequest().setAttribute("pager", createPager(playlist.size(), index));
                } else {
                    getRequest().setAttribute("tracks", playlist);
                }
                List<Track> tracks = (List<Track>)getRequest().getAttribute("tracks");
                if (!tracks.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (Track track : tracks) {
                        builder.append(",").append(track.getId());
                    }
                    getRequest().setAttribute("trackIds", builder.substring(1));
                }
                forward(MyTunesRssResource.EditPlaylist);
            } else {
                addError(new BundleError("error.cannotEditEmptyPlaylist"));
                redirect(MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null)));
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}