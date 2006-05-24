package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class UpdateHelpTablesStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        connection.createStatement().execute("INSERT INTO album ( SELECT DISTINCT(album) AS album, LOWER(SUBSTRING(album, 1, 1)) AS first, COUNT(DISTINCT(id)) AS track_count, COUNT(DISTINCT(artist)) AS artist_count, NULL AS artist FROM track GROUP BY album )");
        connection.createStatement().execute("INSERT INTO artist ( SELECT DISTINCT(artist) AS artist, LOWER(SUBSTRING(artist, 1, 1)) AS first, COUNT(DISTINCT(id)) AS track_count, COUNT(DISTINCT(album)) AS album_count FROM track GROUP BY artist ORDER BY artist )");
        connection.commit();
    }
}