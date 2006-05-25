/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.logging.*;

import java.sql.*;

import de.codewave.mytunesrss.datastore.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertITunesPlaylistStatement
 */
public class InsertMyTunesPlaylistStatement extends InsertPlaylistStatement {
    private static final Log LOG = LogFactory.getLog(InsertMyTunesPlaylistStatement.class);

    public InsertMyTunesPlaylistStatement() {
        setType(PlaylistType.MyTunes);
    }

    public InsertMyTunesPlaylistStatement(DataStoreSession storeSession) throws SQLException {
        super(storeSession);
        setType(PlaylistType.MyTunes);
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        ResultSet result = connection.createStatement().executeQuery("SELECT TOP 1 NEXT VALUE FOR mytunes_playlist_id AS id FROM playlist");
        if (result.next()) {
            setId("MyTunesRSS" + result.getInt("id"));
            super.execute(connection);
        } else {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create next sequence value for mytunesrss playlist.");
            }
        }
    }
}