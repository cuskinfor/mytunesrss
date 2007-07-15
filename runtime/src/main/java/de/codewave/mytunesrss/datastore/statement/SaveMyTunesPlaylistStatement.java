/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import org.apache.commons.lang.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveMyTunesPlaylistStatement extends SavePlaylistStatement {
    public SaveMyTunesPlaylistStatement() {
        setType(PlaylistType.MyTunes);
    }

    public void execute(Connection connection) throws SQLException {
        if (StringUtils.isEmpty(myId)) {
            ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "nextPlaylistId").executeQuery();
            if (resultSet.next()) {
                int playlistId = resultSet.getInt("ID");
                setId("MyTunesRSS" + playlistId);
            }
            executeInsert(connection);
        } else {
            executeUpdate(connection);
        }
    }
}