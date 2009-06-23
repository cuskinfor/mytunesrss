/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.service.EditPlaylistService;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * de.codewave.mytunesrss.command.StartNewPlaylistCommandHandler
 */
public class StartNewPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isCreatePlaylists()) {
            getStates().put("addToPlaylistMode", Boolean.TRUE);
            MyTunesRssRemoteEnv.getSessionForRegularSession(getRequest()).setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST, new Playlist());
            MyTunesRssRemoteEnv.getSessionForRegularSession(getRequest()).setAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS, new ArrayList<Track>());
            String backUrl = MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null));
            if (StringUtils.isNotEmpty(backUrl)) {
                redirect(backUrl);
            } else {
                forward(MyTunesRssCommand.ShowPortal);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}