package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class UpdateHelpTablesStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        connection.createStatement().execute("INSERT INTO album ( SELECT DISTINCT(t1.album) AS album, COUNT(DISTINCT(t1.id)) AS track_count, COUNT(DISTINCT(t1.artist)) AS artist_count, (SELECT TOP 1 artist FROM track WHERE album = t1.album ORDER BY artist) AS artist FROM track t1 GROUP BY t1.album )");
        connection.createStatement().execute("INSERT INTO artist ( SELECT DISTINCT(artist) AS artist, COUNT(DISTINCT(id)) AS track_count, COUNT(DISTINCT(album)) AS album_count FROM track GROUP BY artist ORDER BY artist )");
        connection.commit();
    }
}