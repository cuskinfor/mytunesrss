/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.UserAgent;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * de.codewave.mytunesrss.command.StartCommandHandler
 */
public class StartCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        if ("iphone".equals(getRequest().getParameter("interface"))) {
            redirect(MyTunesRssWebUtils.getServletUrl(getRequest()) + "/" + MyTunesRssCommand.ShowIphoneIndex.getName());
            //forward(MyTunesRssResource.IphoneIndex);
        } else if ("default".equals(getRequest().getParameter("interface")) || MyTunesRssWebUtils.getUserAgent(getRequest()) != UserAgent.Iphone) {
            redirect(MyTunesRssWebUtils.getServletUrl(getRequest()) + "/" + MyTunesRssCommand.ShowPortal.getName());
            //forward(MyTunesRssCommand.ShowPortal);
        } else {
            redirect(MyTunesRssWebUtils.getServletUrl(getRequest()) + "/" + MyTunesRssCommand.ShowIphoneStartup.getName());
        }
    }
}
