package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveM3uFilePlaylistStatement extends SavePlaylistStatement {
    public SaveM3uFilePlaylistStatement() {
        setType(PlaylistType.M3uFile);
    }

    public void execute(Connection connection) throws SQLException {
        ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "nextPlaylistId").executeQuery();
        if (resultSet.next()) {
            int playlistId = resultSet.getInt("ID");
            setId("M3U" + playlistId);
        }
        executeInsert(connection);
    }
}