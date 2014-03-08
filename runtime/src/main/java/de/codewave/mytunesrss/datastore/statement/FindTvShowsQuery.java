/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindTvShowsQuery extends MyTunesRssDataStoreQuery<QueryResult<TvShow>> {

    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private String[] myPermittedDataSources;

    public FindTvShowsQuery(User user) {
        if (user != null) {
            myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
            myExcludedPlaylistIds = user.getExcludedPlaylistIds();
            myPermittedDataSources = FindTrackQuery.getPermittedDataSources(user);
            setForceEmptyResult(!user.isVideo());
        }
    }

    public QueryResult<TvShow> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<>();
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("datasource", myPermittedDataSources != null);
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findTvShows", conditionals);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        statement.setItems("datasources", myPermittedDataSources);
        return execute(statement, new TvShowResultBuilder());
    }

    public static class TvShowResultBuilder implements ResultBuilder<TvShow> {
        private TvShowResultBuilder() {
            // intentionally left blank
        }

        public TvShow create(ResultSet resultSet) throws SQLException {
            TvShow tvShow = new TvShow();
            tvShow.setName(resultSet.getString("SERIES"));
            tvShow.setSeasonCount(resultSet.getInt("SEASON_COUNT"));
            tvShow.setEpisodeCount(resultSet.getInt("EPISODE_COUNT"));
            return tvShow;
        }
    }
}
