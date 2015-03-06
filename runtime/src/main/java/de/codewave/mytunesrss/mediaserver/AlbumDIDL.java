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
import org.fourthline.cling.support.model.container.Container;

import java.sql.SQLException;

public class AlbumDIDL extends MyTunesRssContainerDIDL {

    @Override
    void createDirectChildren(final User user, DataStoreSession tx, final String oidParams, long firstResult, long maxResults) throws SQLException {
        final String album = decode(oidParams).get(0);
        final String artist = decode(oidParams).get(1);
        executeAndProcess(
                tx,
                FindTrackQuery.getForAlbum(user, new String[]{album}, new String[]{artist}, SortOrder.Album),
                new DataStoreQuery.ResultProcessor<Track>() {
                    @Override
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

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        FindAlbumQuery findAlbumQuery = new FindAlbumQuery(
                user, decode(oidParams).get(0), decode(oidParams).get(1), true, null, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, false, FindAlbumQuery.AlbumType.ALL
        );
        findAlbumQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1);
        Album album = tx.executeQuery(findAlbumQuery).nextResult();
        if (album != null) {
            Container container = createSimpleContainer(getParentId(album.getName(), album.getArtist()), ObjectID.Albums.getValue(), album.getTrackCount());
            addUpnpAlbumArtUri(user, album.getImageHash(), container);
            addContainer(container);
            myTotalMatches = 1;
        } else {
            myTotalMatches = 0;
        }
    }
}
