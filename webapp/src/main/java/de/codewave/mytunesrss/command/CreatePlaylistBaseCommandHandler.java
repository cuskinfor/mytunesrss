/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.command.CreatePlaylistCommandHandler
 */
public class CreatePlaylistBaseCommandHandler extends MyTunesRssCommandHandler {
    protected DataStoreQuery.QueryResult<Track> getTracks() throws SQLException, IOException, ServletException {
        String[] trackIds = getNonEmptyParameterValues("track");
        String trackList = getRequestParameter("tracklist", null);
        if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
            trackIds = StringUtils.split(trackList, ',');
        }
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = null;
        if (trackIds != null && trackIds.length > 0) {
            query = FindTrackQuery.getForId(trackIds);
        } else {
            query = TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), true);
        }
        return query != null ? getTransaction().executeQuery(query) : null;
    }
}