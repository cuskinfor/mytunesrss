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
        executeInsert(connection);
    }
}