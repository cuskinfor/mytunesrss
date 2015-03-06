/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssWebUtils;

/**
 * de.codewave.mytunesrss.command.DoLogoutCommandHandler
 */
public class DoLogoutCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        MyTunesRssWebUtils.forgetLogin(getResponse());
        getSession().invalidate();
        redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.Login));
    }
}