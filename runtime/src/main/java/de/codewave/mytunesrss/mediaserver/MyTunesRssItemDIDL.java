/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.item.Item;

import java.sql.SQLException;

public abstract class MyTunesRssItemDIDL extends MyTunesRssDIDL {

    private long myTotalMatches;

    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) {
        // no children available, it is not a container
        myTotalMatches = 0;
    }

    @Override
    void createMetaData(final User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        FindTrackQuery query = FindTrackQuery.getForIds(new String[]{ decode(oidParams).get(0) });
        query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1);
        Item item = createItem(tx.executeQuery(query).nextResult(), user, oidParams);
        if (item != null) {
            addItem(item);
            myTotalMatches = 1;
        } else {
            myTotalMatches = 0;
        }
    }

    protected abstract Item createItem(Track track, User user, String oidParams);

    @Override
    long getTotalMatches() {
        return myTotalMatches;
    }
}
