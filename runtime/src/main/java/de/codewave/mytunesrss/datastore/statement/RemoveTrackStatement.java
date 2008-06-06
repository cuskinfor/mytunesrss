/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
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
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeTrack");
        int count = 0;
        long lastEventTime = 0;
        for (String trackId : myTrackIds) {
            if (System.currentTimeMillis() - lastEventTime >= 2500) {
                MyTunesRssEvent event = MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED;
                event.setMessageKey("settings.databaseUpdateRemovingTracks");
                event.setMessageParams(myTrackIds.size() - count);
                MyTunesRssEventManager.getInstance().fireEvent(event);
                lastEventTime = System.currentTimeMillis();
            }
            statement.setString("track_id", trackId);
            statement.execute();
            if (count++ % 500 == 0) {
                connection.commit();
            }
        }
    }
}