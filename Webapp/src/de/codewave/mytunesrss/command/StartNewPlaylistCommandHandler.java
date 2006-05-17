/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.servlet.*;

import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.StartNewPlaylistCommandHandler
 */
public class StartNewPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        getSession().setAttribute("playlist", new Playlist());
        getSession().setAttribute("playlistContent", new LinkedHashSet<Track>());
        String backUrl = getRequestParameter("backUrl", null);
        if (StringUtils.isNotEmpty(backUrl)) {
            getResponse().sendRedirect(backUrl);
        } else {
            forward(MyTunesRssCommand.BrowseArtist);
        }
    }
}