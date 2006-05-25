/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.datastore.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertITunesPlaylistStatement
 */
public class InsertITunesPlaylistStatement extends InsertPlaylistStatement {
    public InsertITunesPlaylistStatement() {
        setType(PlaylistType.ITunes);
    }

    protected InsertITunesPlaylistStatement(DataStoreSession storeSession) throws SQLException {
        super(storeSession);
        setType(PlaylistType.ITunes);
    }
}
