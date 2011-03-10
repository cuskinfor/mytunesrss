/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery
 */
public class GetSystemInformationQuery extends DataStoreQuery<SystemInformation> {
    private static final Logger LOG = LoggerFactory.getLogger(GetSystemInformationQuery.class);

    public SystemInformation execute(Connection connection) {
        try {
            ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "getSystemInformation").executeQuery();
            if (resultSet.next()) {
                SystemInformation systemInformation = new SystemInformation();
                systemInformation.setLastUpdate(resultSet.getLong("lastupdate"));
                systemInformation.setVersion(resultSet.getString("version"));
                systemInformation.setTrackCount(resultSet.getInt("track_count"));
                systemInformation.setMusicCount(resultSet.getInt("music_count"));
                systemInformation.setMovieCount(resultSet.getInt("movie_count"));
                systemInformation.setTvShowCount(resultSet.getInt("tvshow_count"));
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
