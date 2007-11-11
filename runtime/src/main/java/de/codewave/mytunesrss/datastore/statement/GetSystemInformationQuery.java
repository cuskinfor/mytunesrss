/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery
 */
public class GetSystemInformationQuery extends DataStoreQuery<SystemInformation> {
    private static final Log LOG = LogFactory.getLog(GetSystemInformationQuery.class);

    public SystemInformation execute(Connection connection) {
        try {
            ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "getSystemInformation").executeQuery();
            if (resultSet.next()) {
                SystemInformation systemInformation = new SystemInformation();
                systemInformation.setLastUpdate(resultSet.getLong("lastupdate"));
                systemInformation.setVersion(resultSet.getString("version"));
                systemInformation.setTrackCount(resultSet.getInt("track_count"));
                systemInformation.setAlbumCount(resultSet.getInt("album_count"));
                systemInformation.setArtistCount(resultSet.getInt("artist_count"));
                systemInformation.setGenreCount(resultSet.getInt("genre_count"));
                return systemInformation;
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not get system information.", e);
            }
        }
        return new SystemInformation();
    }
}
