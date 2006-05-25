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
        connection.createStatement().execute("INSERT INTO album ( SELECT DISTINCT(album) AS album, LOWER(SUBSTRING(album, 1, 1)) AS first, COUNT(DISTINCT(id)) AS track_count, COUNT(DISTINCT(artist)) AS artist_count, NULL AS artist FROM track GROUP BY album )");
        connection.createStatement().execute("INSERT INTO artist ( SELECT DISTINCT(artist) AS artist, LOWER(SUBSTRING(artist, 1, 1)) AS first, COUNT(DISTINCT(id)) AS track_count, COUNT(DISTINCT(album)) AS album_count FROM track GROUP BY artist ORDER BY artist )");
        PreparedStatement statement = connection.prepareStatement("UPDATE album SET artist = ? WHERE name = ?");
        for (Map.Entry<String, String> mapping : myAlbumArtistMapping) {
            statement.setString(1, mapping.getValue());
            statement.setString(2, mapping.getKey());
            statement.execute();
        }
    }
}