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
public class FindAllTagsForAlbumQuery extends DataStoreQuery<DataStoreQuery.QueryResult<String>> {
    private String myArtist;
    private String myAlbum;

    public FindAllTagsForAlbumQuery(String artist, String album) {
        myArtist = artist;
        myAlbum = album;
    }

    public QueryResult<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findAllTagsForAlbum");
        statement.setString("artist", myArtist);
        statement.setString("album", myAlbum);
        return execute(statement, new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString(1);
            }
        });
    }
}