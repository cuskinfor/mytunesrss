/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.CreateM3uCommandHandler
 */
public class CreatePlaylistCommandHandler extends MyTunesRssCommandHandler {
    protected Collection<Track> getTracks() throws SQLException, IOException, ServletException {
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
        return getDataStore().executeQuery(query);
    }
}