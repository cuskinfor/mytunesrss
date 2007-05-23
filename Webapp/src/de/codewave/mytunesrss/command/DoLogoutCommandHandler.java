/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.servlet.*;

/**
 * de.codewave.mytunesrss.command.DoLogoutCommandHandler
 */
public class DoLogoutCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        WebConfig webConfig = getWebConfig();
        webConfig.setLoginStored(false);
        webConfig.setPasswordHash(new byte[0]);
        webConfig.save(getRequest(), getResponse());
        String user = (String)getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER);
        getSession().invalidate();
        if (user != null) {
            restartMyTunesRssCom(user, webConfig);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}