/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.command.CreatePlaylistCommandHandler
 */
public class CreatePlaylistBaseCommandHandler extends MyTunesRssCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreatePlaylistBaseCommandHandler.class);

    protected QueryResult<Track> getTracks() throws SQLException {
        String[] trackIds = getNonEmptyParameterValues("track");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Track ids from 'track' parameter: " + StringUtils.join(trackIds, ", "));
        }
        String trackList = getRequestParameter("tracklist", null);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'tracklist' parameter: " + trackList);
        }
        if ((trackIds == null || trackIds.length == 0) && StringUtils.isNotEmpty(trackList)) {
            trackIds = StringUtils.split(trackList, ',');
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Effective track ids: " + StringUtils.join(trackIds, ", "));
        }
        DataStoreQuery<QueryResult<Track>> query = null;
        if (trackIds != null && trackIds.length > 0) {
            query = FindTrackQuery.getForIds(trackIds);
        } else {
            query = TrackRetrieveUtils.getQuery(getTransaction(), getRequest(), getAuthUser(), true);
        }
        return query != null ? getTransaction().executeQuery(query) : null;
    }
}
