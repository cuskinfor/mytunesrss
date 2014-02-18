/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.NotYetImplementedException;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.SortCriterion;

import java.sql.SQLException;

public class AlbumDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        final String album = decode(oidParams).get(0);
        final String artist = decode(oidParams).get(1);
        executeAndProcess(
                tx,
                FindTrackQuery.getForAlbum(user, new String[]{album}, new String[]{artist}, SortOrder.Album),
                new DataStoreQuery.ResultProcessor<Track>() {
                    public void process(Track track) {
                        String id = getObjectId(track);
                        addItem(createMusicTrack(user, track, id, getParentId(album, artist)));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    protected String getParentId(String album, String artist) {
        return ObjectID.Album.getValue() + ";" + encode(album, artist);
    }

    protected String getObjectId(Track track) {
        return ObjectID.AlbumTrack.getValue() + ";" + encode(track.getId());
    }

}
