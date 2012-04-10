/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.statement.FindSmartPlaylistQuery
 */
public class FindSmartPlaylistQuery extends DataStoreQuery<Collection<SmartPlaylist>> {
    private String myId;

    public FindSmartPlaylistQuery(String id) {
        myId = id;
    }

    public Collection<SmartPlaylist> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findSmartPlaylistById");
        statement.setString("id", myId);
        SmartPlaylistResultBuilder builder = new SmartPlaylistResultBuilder();
        execute(statement, builder).getResults();
        return builder.getSmartPlaylists();
    }
}