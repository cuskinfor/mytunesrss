/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import org.fourthline.cling.support.model.SortCriterion;

import java.sql.SQLException;

public class PlaylistDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, final DataStoreSession tx, final String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        FindPlaylistTracksQuery findPlaylistTracksQuery = new FindPlaylistTracksQuery(user, decode(oidParams).get(0), SortOrder.KeepOrder, (int)firstResult, (int)maxResults);
        findPlaylistTracksQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        tx.executeQuery(findPlaylistTracksQuery).processRemainingResults(new DataStoreQuery.ResultProcessor<Track>() {
            @Override
            public void process(Track track) {
                switch (track.getMediaType()) {
                    case Audio:
                        addItem(createMusicTrack(user, track, ObjectID.PlaylistTrack.getValue() + ";" + encode(track.getId()), ObjectID.Playlist.getValue() + ";" + oidParams));
                        break;
                    case Video:
                        addItem(createMovieTrack(user, track, ObjectID.PlaylistTrack.getValue() + ";" + encode(track.getId()), ObjectID.Playlist.getValue() + ";" + oidParams));
                        break;
                    default:
                        // do nothing
                }
            }
        });
        myTotalMatches = tx.executeQuery(new FindPlaylistQuery(user, null, decode(oidParams).get(0), null, true, false)).nextResult().getTrackCount();
    }

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams, String filter, long firstResult, long maxResults, SortCriterion[] orderby) throws SQLException {
        FindPlaylistQuery findPlaylistQuery = new FindPlaylistQuery(user, null, decode(oidParams).get(0), null, false, false);
        findPlaylistQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1);
        Playlist playlist = tx.executeQuery(findPlaylistQuery).nextResult();
        int childCount = playlist.getTrackCount();
        String parentId = playlist.getContainerId() != null ? ObjectID.PlaylistFolder.getValue() + ";" + encode(playlist.getContainerId()) : ObjectID.Root.getValue();
        addContainer(createSimpleContainer(ObjectID.PlaylistFolder.getValue() + ";" + oidParams, parentId, childCount));
        myTotalMatches = 1;
    }

}
