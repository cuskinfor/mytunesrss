package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class UpdateHelpTablesStatement implements DataStoreStatement {
    Collection<Map.Entry<String, String>> myAlbumArtistMapping;

    public UpdateHelpTablesStatement(Collection<Map.Entry<String, String>> albumArtistMapping) {
        myAlbumArtistMapping = albumArtistMapping;
    }

    public void execute(Connection connection) throws SQLException {
        connection.createStatement().execute("INSERT INTO album (name, first_char, track_count, artist_count) SELECT DISTINCT(album), LOWER(SUBSTR(album, 1, 1)), COUNT(id), COUNT(DISTINCT(artist)) FROM track GROUP BY album");
        connection.createStatement().execute("INSERT INTO artist SELECT DISTINCT(artist), LOWER(SUBSTR(artist, 1, 1)), COUNT(id), COUNT(DISTINCT(album)) FROM track GROUP BY artist");
        PreparedStatement statement = connection.prepareStatement("UPDATE album SET artist = ? WHERE name = ?");
        for (Map.Entry<String, String> mapping : myAlbumArtistMapping) {
            statement.setString(1, mapping.getValue());
            statement.setString(2, mapping.getKey());
            statement.execute();
        }
    }
}