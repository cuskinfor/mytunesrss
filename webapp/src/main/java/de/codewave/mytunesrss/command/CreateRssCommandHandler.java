/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.MiscUtils;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * de.codewave.mytunesrss.command.CreateRssCommandHandler
 */
public class CreateRssCommandHandler extends CreatePlaylistBaseCommandHandler {
    private static final SimpleDateFormat PUBLISH_DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);

    @Override
    public void executeAuthorized() throws Exception {
        if (getAuthUser().isRss()) {
            String feedUrl = getRequest().getRequestURL().toString();
            String channel = feedUrl.substring(feedUrl.lastIndexOf('/') + 1);
            channel = channel.substring(0, channel.lastIndexOf('.'));
            getRequest().setAttribute("channel", MiscUtils.decodeUrl(channel.replace('_', ' ')));
            getRequest().setAttribute("pubDate", PUBLISH_DATE_FORMAT.format(new Date()));
            getRequest().setAttribute("feedUrl", feedUrl);
            getRequest().setAttribute("userAgentPsp", isUserAgentPSP());
            Collection<Track> tracks = getTracks().getResults();
            if (tracks != null && !tracks.isEmpty()) {
                for (Track track : tracks) {
                    if (track.getImageCount() > 0) {
                        getRequest().setAttribute("imageTrackId", track.getId());
                        break;// use first available image
                    }
                }
                getRequest().setAttribute("tracks", tracks);
                forward(MyTunesRssResource.TemplateRss);
            } else {
                addError(new BundleError("error.emptyFeed"));
                forward(MyTunesRssCommand.ShowPortal);// todo: redirect to backUrl
            }
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
}