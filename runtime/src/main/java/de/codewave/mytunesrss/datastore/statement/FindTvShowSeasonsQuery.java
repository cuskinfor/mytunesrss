/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindTvShowSeasonsQuery extends MyTunesRssDataStoreQuery<QueryResult<TvShowSeason>> {

    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private String[] myPermittedDataSources;
    private String mySeries;

    public FindTvShowSeasonsQuery(User user, String series) {
        if (user != null) {
            myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
            myExcludedPlaylistIds = user.getExcludedPlaylistIds();
            myPermittedDataSources = FindTrackQuery.getPermittedDataSources(user);
            setForceEmptyResult(!user.isVideo());
        }
        mySeries = series;
    }

    public QueryResult<TvShowSeason> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<>();
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("datasource", myPermittedDataSources != null);
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findTvShowSeasons", conditionals);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        statement.setItems("datasources", myPermittedDataSources);
        statement.setString("series", mySeries);
        return execute(statement, new TvShowSeasonResultBuilder());
    }

    public static class TvShowSeasonResultBuilder implements ResultBuilder<TvShowSeason> {
        private TvShowSeasonResultBuilder() {
            // intentionally left blank
        }

        public TvShowSeason create(ResultSet resultSet) throws SQLException {
            TvShowSeason season = new TvShowSeason();
            season.setNumber(resultSet.getInt("SEASON"));
            season.setEpisodeCount(resultSet.getInt("EPISODE_COUNT"));
            return season;
        }
    }
}
