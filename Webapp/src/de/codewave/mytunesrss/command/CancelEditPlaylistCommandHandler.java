/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.CancelEditPlaylistCommandHandler
 */
public class CancelEditPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        getStates().put("addToPlaylistMode", Boolean.FALSE);
        String backUrl = getRequestParameter("backUrl", null);
        if (StringUtils.isNotEmpty(backUrl)) {
            redirect(backUrl);
        } else {
            forward(MyTunesRssCommand.ShowPortal);
        }
    }
}