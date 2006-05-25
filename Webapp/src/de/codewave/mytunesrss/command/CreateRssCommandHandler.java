/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;

import java.text.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.CreateRssCommandHandler
 */
public class CreateRssCommandHandler extends CreatePlaylistCommandHandler {
    private SimpleDateFormat myPublishDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);

    @Override
    public void executeAuthorized() throws Exception {
        String pathInfo = getRequest().getPathInfo();
        String feedUrlPathInfo = pathInfo.substring(1, pathInfo.lastIndexOf("/"));
        if (!feedUrlPathInfo.contains("authHash=")) {
            feedUrlPathInfo += "/authHash=" + getAuthHash();
        }
        String channel = pathInfo.substring(pathInfo.lastIndexOf('/') + 1);
        feedUrlPathInfo += "/" + channel;
        channel = channel.substring(0, channel.lastIndexOf('.'));
        getRequest().setAttribute("channel", channel.replace('_', ' '));
        getRequest().setAttribute("pubDate", myPublishDateFormat.format(new Date()));
        getRequest().setAttribute("pathInfo", feedUrlPathInfo);
        createDataAndForward(MyTunesRssResource.TemplateRss);
    }
}