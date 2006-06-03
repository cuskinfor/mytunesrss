package de.codewave.mytunesrss.datastore.statement;

import java.util.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumArtistMappingQuery
 */
public class FindAlbumArtistMappingQuery extends DataStoreQuery<Set<Map.Entry<String, String>>> {
    public Set<Map.Entry<String, String>> execute(Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT album AS album, artist AS artist FROM track");
        Map<String, String> mapping = new HashMap<String, String>();
        while (resultSet.next()) {
            String album = resultSet.getString("ALBUM");
            mapping.put(album, resultSet.getString("ARTIST"));
        }
        return mapping.entrySet();
    }
}