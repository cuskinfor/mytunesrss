/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.DIDLContent;

public abstract class MyTunesRssDIDLContent extends DIDLContent {

    final void init() throws Exception {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            create(MyTunesRss.CONFIG.getUser("cling"), tx);
        } finally {
            tx.rollback();
        }
    }

    abstract void create(User user, DataStoreSession tx) throws Exception;

    abstract long getTotalMatches();

}
