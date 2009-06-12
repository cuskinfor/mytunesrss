/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.statement.RemoveTrackStatement
 */
public class RemoveTrackStatement implements DataStoreStatement {
    private Collection<String> myTrackIds;

    public RemoveTrackStatement(Collection<String> trackIds) {
        myTrackIds = trackIds;
    }

    public void execute(Connection connection) throws SQLException {
        MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
        event.setMessageKey("settings.databaseUpdateRemovingTracks");
        event.setMessageParams(myTrackIds.size());
        MyTunesRssEventManager.getInstance().fireEvent(event);
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeTrack");
        statement.setObject("track_id", myTrackIds);
        statement.execute();
    }
}