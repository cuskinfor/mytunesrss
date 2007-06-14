/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.*;

/**
 * de.codewave.mytunesrss.command.ShowSettingsCommandHandler
 */
public class ShowSettingsCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            getRequest().setAttribute("themes", AddonsUtils.getThemes());
            forward(MyTunesRssResource.Settings);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}