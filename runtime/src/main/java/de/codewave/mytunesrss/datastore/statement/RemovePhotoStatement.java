/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
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
 * de.codewave.mytunesrss.datastore.statement.RemovePhotoStatement
 */
public class RemovePhotoStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemovePhotoStatement.class);

    private Collection<String> myTrackIds;

    public RemovePhotoStatement(Collection<String> trackIds) {
        myTrackIds = trackIds;
    }

    public void execute(Connection connection) throws SQLException {
        MyTunesRssEvent event = MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_STATE_CHANGED, "event.databaseUpdateRemovingPhotos");
        MyTunesRssEventManager.getInstance().fireEvent(event);
        MyTunesRss.LAST_DATABASE_EVENT = event;
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removePhoto");
        statement.setObject("photo_id", myTrackIds);
        statement.execute();
    }
}