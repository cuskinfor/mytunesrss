/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.command.AddToPlaylistCommandHandler
 */
public class AddToPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            Collection<Track> playlist = (Collection<Track>)getSession().getAttribute("playlistContent");
            String[] trackIds = getNonEmptyParameterValues("track");
            String trackList = getRequestParameter("tracklist", null);
            if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
                trackIds = StringUtils.split(trackList, ',');
            }
            DataStoreQuery<Collection<Track>> query = null;
            if (trackIds != null && trackIds.length > 0) {
                query = FindTrackQuery.getForId(trackIds);
            } else {
                query = TrackRetrieveUtils.getQuery(getRequest(), true);
            }
            if (query != null) {
                playlist.addAll(getDataStore().executeQuery(query));
                ((Playlist)getSession().getAttribute("playlist")).setTrackCount(playlist.size());
            } else {
                addError(new BundleError("error.emptySelection"));
            }
            String backUrl = MyTunesRssBase64Utils.decodeToString(getRequestParameter("backUrl", null));
            if (StringUtils.isNotEmpty(backUrl)) {
                redirect(backUrl);
            } else {
                forward(MyTunesRssCommand.ShowPortal);
            }
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}