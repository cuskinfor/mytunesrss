/*
 * Copyright (c) 2009. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.utils.sql.DataStoreSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class RefreshSmartPlaylistsAndLuceneIndexCallable implements Callable<Void> {

    private String[] trackIds;

    public RefreshSmartPlaylistsAndLuceneIndexCallable(String[] trackIds) {
        this.trackIds = trackIds;
    }

    public Void call() throws IOException, SQLException {
        MyTunesRss.LUCENE_TRACK_SERVICE.updateTracks(trackIds);
        DataStoreSession transaction = MyTunesRss.STORE.getTransaction();
        try {
            transaction.executeStatement(new RefreshSmartPlaylistsStatement());
            transaction.commit();
        } finally {
            transaction.rollback();
        }
        return null;
    }
}
