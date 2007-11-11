package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumArtistMappingQuery
 */
public class FindAlbumArtistMappingQuery extends DataStoreQuery<Map<String, String>> {
    public Map<String, String> execute(Connection connection) throws SQLException {
        ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "findAlbumArtistMapping").executeQuery();
        Map<String, String> mapping = new HashMap<String, String>();
        while (resultSet.next()) {
            String album = resultSet.getString("ALBUM");
            mapping.put(album, resultSet.getString("ARTIST"));
        }
        return mapping;
    }
}