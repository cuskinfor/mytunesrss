/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.service.EditPlaylistService;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.command.CancelCreatePlaylistCommandHandler
 */
public class CancelCreatePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            getStates().put("addToPlaylistMode", Boolean.FALSE);
            MyTunesRssRemoteEnv.getSessionForRegularSession(getRequest()).removeAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST_TRACKS);
            MyTunesRssRemoteEnv.getSessionForRegularSession(getRequest()).removeAttribute(EditPlaylistService.KEY_EDIT_PLAYLIST);
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