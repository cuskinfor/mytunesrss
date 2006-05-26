/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.datastore.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveITunesPlaylistStatement extends SavePlaylistStatement {
    public SaveITunesPlaylistStatement() {
        setType(PlaylistType.ITunes);
    }

    protected SaveITunesPlaylistStatement(DataStoreSession storeSession) throws SQLException {
        super(storeSession);
        setType(PlaylistType.ITunes);
    }


    public void execute(Connection connection) throws SQLException {
        executeInsert(connection);
    }
}
