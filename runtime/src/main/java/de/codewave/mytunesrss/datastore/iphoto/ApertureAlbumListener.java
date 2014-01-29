/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.config.PhotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;

import java.sql.SQLException;
import java.util.Map;

public class ApertureAlbumListener extends IphotoAlbumListener {
    public ApertureAlbumListener(PhotoDatasourceConfig datasourceConfig, Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<String, String> photoIdToPersId) throws SQLException {
        super(datasourceConfig, watchdogThread, queue, libraryListener, photoIdToPersId);
    }

    @Override
    protected boolean useAlbum(String albumType) {
        return "4".equals(albumType);
    }
}
