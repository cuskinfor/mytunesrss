/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveMyTunesPlaylistStatement extends SavePlaylistStatement {
    public SaveMyTunesPlaylistStatement(String userName, boolean userPrivate) {
        super(null);
        setType(PlaylistType.MyTunes);
        setUserName(userName);
        setUserPrivate(userPrivate);
    }

    public void execute(Connection connection) throws SQLException {
        if (StringUtils.isEmpty(myId)) {
            ResultSet resultSet = MyTunesRssUtils.createStatement(connection, "nextPlaylistId").executeQuery();
            if (resultSet.next()) {
                int playlistId = resultSet.getInt("ID");
                setId("MyTunesRSS" + playlistId);
            }
        } else {
            setUpdate(true);
        }
        super.execute(connection);
    }
}
