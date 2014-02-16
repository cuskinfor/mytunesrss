/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.protocol.sync.ReceivingAction;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.fourthline.cling.transport.impl.HttpExchangeUpnpStream;

public class AlbumDIDL extends MyTunesRssDIDLContent {
    private long myTotalMatches;

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws Exception {
        final String album = decode(oidParams).get(0);
        final String artist = decode(oidParams).get(1);
        final String parentID = ObjectID.Album.getValue() + ";" + encode(album, artist);
        myTotalMatches = executeAndProcess(
                tx,
                FindTrackQuery.getForAlbum(user, new String[]{album}, new String[]{artist}, SortOrder.Album),
                new DataStoreQuery.ResultProcessor<Track>() {
                    public void process(Track track) {
                        String id = ObjectID.AlbumTrack.getValue() + ";" + encode(track.getId());
                        addItem(new MusicTrack(id, parentID, track.getName(), "MyTunesRSS", track.getAlbum(), track.getArtist(), createTrackResource(track, user)));
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
