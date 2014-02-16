/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.item.MusicTrack;

public class AlbumDIDL extends MyTunesRssDIDLContent {
    private long myTotalMatches;

    @Override
    void createDirectChildren(User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        final String album = decode(oidParams).get(0);
        final String artist = decode(oidParams).get(1);
        final String parentID = ObjectID.Album.getValue() + ";" + encode(album, artist);
        myTotalMatches = executeAndProcess(
                tx,
                FindTrackQuery.getForAlbum(user, new String[]{album}, new String[]{artist}, SortOrder.KeepOrder),
                new DataStoreQuery.ResultProcessor<Track>() {
                    public void process(Track track) {
                        String id = ObjectID.AlbumTrack.getValue() + ";" + encode(track.getId());
                        Res res = new Res(track.getContentType(), track.getContentLength(), toHumanReadableTime(track.getTime()), 0L, "");
                        addItem(new MusicTrack(id, parentID, track.getName(), "MyTunesRSS", track.getAlbum(), track.getArtist(), res));
                    }
                },
                firstResult,
                (int) maxResults
        );
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    long getTotalMatches() {
        return myTotalMatches;
    }
}
