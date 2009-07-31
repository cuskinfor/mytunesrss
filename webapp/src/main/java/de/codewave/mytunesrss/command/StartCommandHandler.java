/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.UserAgent;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.command.StartCommandHandler
 */
public class StartCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        if (MyTunesRssWebUtils.getUserAgent(getRequest()) == UserAgent.Iphone) {
            redirect(MyTunesRssWebUtils.getServletUrl(getRequest()) + "/" + MyTunesRssCommand.ShowIphoneIndex.getName());
            //forward(MyTunesRssResource.IphoneIndex);
        } else {
            redirect(MyTunesRssWebUtils.getServletUrl(getRequest()) + "/" + MyTunesRssCommand.ShowPortal.getName());
            //forward(MyTunesRssCommand.ShowPortal);
        }
    }
}
