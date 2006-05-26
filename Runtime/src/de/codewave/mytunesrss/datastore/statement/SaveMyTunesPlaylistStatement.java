/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.logging.*;
import org.apache.commons.lang.*;

import java.sql.*;

import de.codewave.mytunesrss.datastore.*;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveMyTunesPlaylistStatement extends SavePlaylistStatement {
    private static final Log LOG = LogFactory.getLog(SaveMyTunesPlaylistStatement.class);

    public SaveMyTunesPlaylistStatement() {
        setType(PlaylistType.MyTunes);
    }

    public SaveMyTunesPlaylistStatement(DataStoreSession storeSession) throws SQLException {
        super(storeSession);
        setType(PlaylistType.MyTunes);
    }

    public void execute(Connection connection) throws SQLException {
        if (StringUtils.isEmpty(myId)) {
            ResultSet result = connection.createStatement().executeQuery("SELECT TOP 1 NEXT VALUE FOR mytunes_playlist_id AS id FROM playlist");
            if (result.next()) {
                setId("MyTunesRSS" + result.getInt("id"));
                executeInsert(connection);
            } else {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create next sequence value for mytunesrss playlist.");
                }
            }
        } else {
            executeUpdate(connection);
        }
    }
}