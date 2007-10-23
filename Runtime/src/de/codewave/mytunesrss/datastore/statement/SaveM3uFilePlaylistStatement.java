package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveM3uFilePlaylistStatement extends SavePlaylistStatement {
    public SaveM3uFilePlaylistStatement() {
        setType(PlaylistType.M3uFile);
    }

    protected SaveM3uFilePlaylistStatement(DataStoreSession storeSession) throws SQLException {
        super(storeSession);
        setType(PlaylistType.M3uFile);
    }

    public void execute(Connection connection) throws SQLException {
        ResultSet resultSet = connection.createStatement().executeQuery(
                "SELECT TOP 1 NEXT VALUE FOR playlist_id_sequence AS id FROM system_information");
        if (resultSet.next()) {
            setId("M3U" + resultSet.getInt("ID"));
        }
        executeInsert(connection);
    }
}