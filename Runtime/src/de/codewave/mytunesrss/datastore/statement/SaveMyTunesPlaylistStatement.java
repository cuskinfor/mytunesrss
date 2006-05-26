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
            ResultSet result = connection.createStatement().executeQuery("SELECT next_playlist_id FROM mytunesrss FOR UPDATE OF next_playlist_id");
            if (result.next()) {
                int id = result.getInt("NEXT_PLAYLIST_ID");
                setId("MyTunesRSS" + id);
                connection.createStatement().execute("UPDATE mytunesrss SET next_playlist_id = " + (id + 1) + " WHERE CURRENT OF " + result.getCursorName());
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