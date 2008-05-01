/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.command.ContinueExistingPlaylistCommandHandler
 */
public class ContinueExistingPlaylistCommandHandler extends LoadPlaylistCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isCreatePlaylists()) {
            loadPlaylist();
            getStates().put("addToPlaylistMode", Boolean.TRUE);
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