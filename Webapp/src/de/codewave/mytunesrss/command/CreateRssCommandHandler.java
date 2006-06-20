/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;

import java.text.*;
import java.util.*;
import java.net.*;

/**
 * de.codewave.mytunesrss.command.CreateRssCommandHandler
 */
public class CreateRssCommandHandler extends CreatePlaylistCommandHandler {
    private SimpleDateFormat myPublishDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);

    @Override
    public void executeAuthorized() throws Exception {
        String feedUrl = getRequest().getRequestURL().toString();
        String channel = feedUrl.substring(feedUrl.lastIndexOf('/') + 1);
        channel = channel.substring(0, channel.lastIndexOf('.'));
        getRequest().setAttribute("channel", URLDecoder.decode(channel.replace('_', ' '), "UTF-8"));
        getRequest().setAttribute("pubDate", myPublishDateFormat.format(new Date()));
        getRequest().setAttribute("feedUrl", feedUrl);
        createDataAndForward(MyTunesRssResource.TemplateRss);
    }
}