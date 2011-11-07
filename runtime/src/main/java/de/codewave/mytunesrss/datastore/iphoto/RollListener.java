/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.itunes.PlaylistListenerr
 */
public class RollListener extends AlbumListener {

    public RollListener(Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<Long, String> photoIdToPersId) throws SQLException {
        super(watchdogThread, queue, libraryListener, photoIdToPersId);
    }


    @Override
    protected String getAlbumName(Map roll) {
        return (String) roll.get("RollName");
    }

    @Override
    protected String getAlbumId(Map roll) {
        if (myLibraryListener.getLibraryId() != null) {
            return myLibraryListener.getLibraryId() + "_" + roll.get("RollID");
        }
        return null;
    }
}
