/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.command.CreateRssCommandHandler
 */
public class CreateRssCommandHandler extends CreatePlaylistBaseCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (getAuthUser().isRss()) {
            String feedUrl = getRequest().getRequestURL().toString();
            getRequest().setAttribute("channel", getContentDispositionFilename());
            getRequest().setAttribute("feedUrl", feedUrl);
            Collection<Track> tracks = getTracks().getResults();
            if (tracks != null && !tracks.isEmpty()) {
                for (Track track : tracks) {
                    if (track.getImageHash() != null) {
                        getRequest().setAttribute("imageHash", track.getImageHash());
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
            getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
