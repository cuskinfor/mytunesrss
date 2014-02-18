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
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.fourthline.cling.support.model.item.MusicTrack;

public class AlbumDIDL extends MyTunesRssDIDLContent {
    private long myTotalMatches;

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        final String album = decode(oidParams).get(0);
        final String artist = decode(oidParams).get(1);
        myTotalMatches = executeAndProcess(
                tx,
                FindTrackQuery.getForAlbum(user, new String[]{album}, new String[]{artist}, SortOrder.Album),
                new DataStoreQuery.ResultProcessor<Track>() {
                    public void process(Track track) {
                        String id = getObjectId(track);
                        addItem(new MusicTrack(id, getParentId(album, artist), track.getName(), track.getArtist(), track.getAlbum(), track.getArtist(), createTrackResource(track, user)));
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

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws Exception {
        throw new NotYetImplementedException();
    }

    @Override
    long getTotalMatches() {
        return myTotalMatches;
    }
}
