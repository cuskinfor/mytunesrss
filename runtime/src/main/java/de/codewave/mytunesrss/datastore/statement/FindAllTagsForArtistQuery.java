package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAllTagsForPlaylistQuery
 */
public class FindAllTagsForArtistQuery extends DataStoreQuery<DataStoreQuery.QueryResult<String>> {
    private String myArtist;

    public FindAllTagsForArtistQuery(String artist) {
        myArtist = artist;
    }

    public QueryResult<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAllTagsForArtist");
        statement.setString("artist", myArtist);
        return execute(statement, new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString(1);
            }
        });
    }
}