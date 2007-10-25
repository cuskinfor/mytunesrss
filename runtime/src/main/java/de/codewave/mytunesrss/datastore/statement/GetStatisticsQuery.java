package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;

import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistIdsQuery
 */
public class GetStatisticsQuery extends DataStoreQuery<Statistics> {

    public Statistics execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getStatistics");
        Collection<Statistics> statistics = execute(statement, new ResultBuilder<Statistics>() {
            public Statistics create(ResultSet resultSet) throws SQLException {
                Statistics statistics = new Statistics();
                statistics.setTrackCount(resultSet.getInt("TRACKCOUNT"));
                statistics.setAlbumCount(resultSet.getInt("ALBUMCOUNT"));
                statistics.setArtistCount(resultSet.getInt("ARTISTCOUNT"));
                statistics.setGenreCount(resultSet.getInt("GENRECOUNT"));
                return statistics;
            }
        }).getResults();
        return statistics != null && !statistics.isEmpty() ? statistics.iterator().next() : null;
    }
}