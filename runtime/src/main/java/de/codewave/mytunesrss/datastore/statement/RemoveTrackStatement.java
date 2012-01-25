/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.statement.RemoveTrackStatement
 */
public class RemoveTrackStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveTrackStatement.class);

    private Collection<String> myTrackIds;

    public RemoveTrackStatement(Collection<String> trackIds) {
        myTrackIds = trackIds;
    }

    public void execute(Connection connection) throws SQLException {
        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRemovingTracks");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        MyTunesRss.LAST_DATABASE_EVENT = event;
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeTrack");
        statement.setObject("track_id", myTrackIds);
        statement.execute();
    }
}