/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.jsp.*;

/**
 * de.codewave.mytunesrss.command.DoLogoutCommandHandler
 */
public class DoLogoutCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        WebConfig webConfig = getWebConfig();
        webConfig.setRememberLogin(false);
        webConfig.save(getResponse());
        getSession().invalidate();
        forward(MyTunesRssResource.Login);
    }
}