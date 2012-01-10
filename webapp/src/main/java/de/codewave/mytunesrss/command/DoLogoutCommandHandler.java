/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.servlet.WebConfig;

/**
 * de.codewave.mytunesrss.command.DoLogoutCommandHandler
 */
public class DoLogoutCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        MyTunesRssWebUtils.forgetLogin(getRequest(), getResponse());
        if (getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) != null) {
            restartMyTunesRssCom();
            getSession().invalidate();
        } else {
            getSession().invalidate();
            redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.Login));
        }
    }
}