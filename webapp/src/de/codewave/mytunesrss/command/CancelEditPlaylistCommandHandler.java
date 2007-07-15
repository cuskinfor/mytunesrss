/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import org.apache.commons.lang.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.*;

/**
 * de.codewave.mytunesrss.command.CancelEditPlaylistCommandHandler
 */
public class CancelEditPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            getStates().put("addToPlaylistMode", Boolean.FALSE);
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