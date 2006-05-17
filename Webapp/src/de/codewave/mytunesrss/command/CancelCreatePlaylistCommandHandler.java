/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.CancelCreatePlaylistCommandHandler
 */
public class CancelCreatePlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        getSession().removeAttribute("playlist");
        getSession().removeAttribute("playlistContent");
        String backUrl = getRequestParameter("backUrl", null);
        if (StringUtils.isNotEmpty(backUrl)) {
            getResponse().sendRedirect(backUrl);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}