/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.EditPlaylistCommandHandler
 */
public class EditPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        Collection<Track> playlist = (Collection<Track>)getSession().getAttribute("playlistContent");
        if ((playlist != null && !playlist.isEmpty()) || getBooleanRequestParameter("allowEditEmpty", false)) {
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && playlist.size() > pageSize) {
                int index = getSafeIntegerRequestParameter("index", 0);
                getRequest().setAttribute("tracks", new ArrayList<Track>(playlist).subList(index * pageSize, Math.min((index * pageSize) + pageSize,
                                                                                                                      playlist.size())));
                getRequest().setAttribute("pager", createPager(playlist.size(), index));
            } else {
                getRequest().setAttribute("tracks", playlist);
            }
            forward(MyTunesRssResource.EditPlaylist);
        } else {
            addError(new BundleError("error.cannotEditEmptyPlaylist"));
            redirect(getRequestParameter("backUrl", null));
        }
    }
}