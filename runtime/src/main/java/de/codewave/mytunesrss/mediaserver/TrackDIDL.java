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
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.item.Item;

/**
 * Created by mdescher on 17.02.14.
 */
public abstract class TrackDIDL extends MyTunesRssDIDLContent {
    @Override
    void createDirectChildren(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        // no children available, it is not a container
    }

    @Override
    void createMetaData(final User user, DataStoreSession tx, String oidParams) throws Exception {
        final String track = decode(oidParams).get(0);
        executeAndProcess(
                tx,
                FindTrackQuery.getForIds(new String[]{track}),
                new DataStoreQuery.ResultProcessor<Track>() {
                    public void process(Track track) {
                        String id = getObjectId(track);
                        String parentID = getParentId(track);
                        addItem(createTrackItem(track, id, parentID, user));
                    }
                },
                0,
                1
        );
    }

    protected abstract Item createTrackItem(Track track, String id, String parentID, User user);

    protected abstract String getParentId(Track track);

    protected abstract String getObjectId(Track track);

    @Override
    long getTotalMatches() {
        // no children available, it is not a container
        return 0;
    }
}
