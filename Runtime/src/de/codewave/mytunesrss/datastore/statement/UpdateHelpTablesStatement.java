package de.codewave.mytunesrss.datastore.statement;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class UpdateHelpTablesStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
//        connection.createStatement().execute("INSERT INTO album ( SELECT DISTINCT(t1.album) AS album, COUNT(distinct(t2.id)) AS track_count, COUNT(DISTINCT(t2.artist)) AS artist_count, NULL AS artist FROM track t1, track t2 WHERE album = t2.album GROUP BY album HAVING COUNT(DISTINCT(t2.artist)) > 1 UNION SELECT DISTINCT(t1.album) AS album, COUNT(distinct(t2.id)) AS track_count, 1 AS artist_count, t2.artist AS artist FROM track t1, track t2 WHERE album = t2.album GROUP BY album, artist HAVING COUNT(DISTINCT(t2.artist)) = 1 )");
        connection.createStatement().execute("INSERT INTO album ( SELECT DISTINCT(t1.album) AS album, COUNT(distinct(t2.id)) AS track_count, NULL AS artist, TRUE AS various FROM track t1, track t2 WHERE album = t2.album GROUP BY album HAVING COUNT(DISTINCT(t2.artist)) > 1 UNION SELECT DISTINCT(t1.album) AS album, COUNT(distinct(t2.id)) AS track_count, t2.artist AS artist, FALSE AS various FROM track t1, track t2, track t3 WHERE t1.album = t2.album AND t1.album = t3.album GROUP BY album, artist HAVING COUNT(DISTINCT(t3.artist)) = 1 )");
        connection.createStatement().execute("INSERT INTO artist ( SELECT DISTINCT(t1.artist) AS artist, COUNT(DISTINCT(t2.id)) AS track_count, COUNT(DISTINCT(t2.album)) AS album_count FROM track t1, track t2 WHERE t1.artist = t2.artist GROUP BY artist ORDER BY artist )");
        connection.commit();
    }
}