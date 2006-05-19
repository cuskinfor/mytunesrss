/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.jsp.*;

/**
 * de.codewave.mytunesrss.command.ShowSettingsCommandHandler
 */
public class ShowSettingsCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        WebConfig webConfig = new WebConfig();
        webConfig.load(getRequest());
        getRequest().setAttribute("config", webConfig);
        forward(MyTunesRssResource.Settings);
    }
}