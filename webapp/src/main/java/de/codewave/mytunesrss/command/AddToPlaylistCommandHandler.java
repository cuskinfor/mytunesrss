/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.command.AddToPlaylistCommandHandler
 */
public class AddToPlaylistCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized()) {
            Collection<Track> playlist = (Collection<Track>)getSession().getAttribute("playlistContent");
            DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = getQuery();
            if (query != null) {
                playlist.addAll(getTransaction().executeQuery(query).getResults());
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

    protected DataStoreQuery<DataStoreQuery.QueryResult<Track>> getQuery() throws SQLException {
        String[] trackIds = getNonEmptyParameterValues("track");
        String trackList = getRequestParameter("tracklist", null);
        if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
            trackIds = StringUtils.split(trackList, ',');
        }
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = null;
        if (trackIds != null && trackIds.length > 0) {
            return FindTrackQuery.getForId(trackIds);
        }
        return TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), true);
    }
}