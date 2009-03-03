/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.EditPlaylistCommandHandler
 */
public class EditPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            Collection<Track> playlist = (Collection<Track>)getSession().getAttribute("playlistContent");
            if ((playlist != null && !playlist.isEmpty()) || getBooleanRequestParameter("allowEditEmpty", false)) {
                if (!Boolean.TRUE.equals(getStates().get("addToPlaylistMode"))) {
                    playlist = filterTracks(playlist);
                }
                int pageSize = getWebConfig().getEffectivePageSize();
                if (pageSize > 0 && playlist.size() > pageSize) {
                    int index = getValidIndex(getSafeIntegerRequestParameter("index", 0), pageSize, playlist.size());
                    getRequest().setAttribute("tracks", new ArrayList<Track>(playlist).subList(index * pageSize, Math.min(
                            (index * pageSize) + pageSize, playlist.size())));
                    getRequest().setAttribute("pager", createPager(playlist.size(), index));
                } else {
                    getRequest().setAttribute("tracks", playlist);
                }
                Collection<Track> tracks = (Collection<Track>)getRequest().getAttribute("tracks");
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

    protected List<Track> filterTracks(Collection<Track> tracks) {
        List<Track> filtered = new ArrayList<Track>();
        for (Track track : tracks) {
            if (matchesFilter(track)) {
                filtered.add(track);
            }
        }
        return filtered;
    }

    private boolean matchesFilter(Track track) {
        DisplayFilter filter = getDisplayFilter();
        if (filter != null) {
            if (StringUtils.isNotEmpty(filter.getTextFilter())) {
                String lowerCaseFilterText = filter.getTextFilter().toLowerCase();
                if (!track.getName().toLowerCase().contains(lowerCaseFilterText) && !track.getAlbum().toLowerCase().contains(lowerCaseFilterText)) {
                    return false;
                }
            }
            if (filter.getMediaType() != null && filter.getMediaType() != track.getMediaType()) {
                return false;
            }
            if (filter.getProtection() == DisplayFilter.Protection.Protected && !track.isProtected()) {
                return false;
            }
            if (filter.getProtection() == DisplayFilter.Protection.Unprotected && track.isProtected()) {
                return false;
            }
        }
        return true;
    }
}