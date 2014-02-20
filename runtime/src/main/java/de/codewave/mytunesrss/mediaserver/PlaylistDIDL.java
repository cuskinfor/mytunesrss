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
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.PlaylistContainer;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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

}
