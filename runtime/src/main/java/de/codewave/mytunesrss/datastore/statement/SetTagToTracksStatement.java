/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class SetTagToTracksStatement implements DataStoreStatement {

    private String[] myTrackIds;
    private String myTag;

    public SetTagToTracksStatement(String[] trackIds, String tag) {
        myTrackIds = trackIds;
        myTag = tag;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "setTagToTracks");
        statement.setString("tag", myTag);
        statement.setObject("track_id", Arrays.asList(myTrackIds));
        statement.execute();
    }
}