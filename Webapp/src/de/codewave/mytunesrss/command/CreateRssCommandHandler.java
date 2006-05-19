/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;

/**
 * de.codewave.mytunesrss.command.CreateRssCommandHandler
 */
public class CreateRssCommandHandler extends CreatePlaylistCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        getRequest().setAttribute("feedUrl", getRequest().getRequestURI());
        String uri = getRequest().getRequestURI();
        String channel = uri.substring(uri.lastIndexOf('/') + 1);
        channel = channel.substring(0, channel.lastIndexOf('.'));
        getRequest().setAttribute("channel", channel.replace('_', ' '));
        createDataAndForward(MyTunesRssResource.TemplateRss);
    }
}