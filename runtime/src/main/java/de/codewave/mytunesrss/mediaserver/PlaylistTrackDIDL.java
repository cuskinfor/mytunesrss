/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.sql.DataStoreSession;
import org.fourthline.cling.support.model.item.Item;

import java.sql.SQLException;

public class PlaylistTrackDIDL extends MyTunesRssItemDIDL {

    private String oidParams;

    @Override
    void createMetaData(User user, DataStoreSession tx, String oidParams) throws SQLException {
        this.oidParams = oidParams;
        super.createMetaData(user, tx, oidParams);
    }

    @Override
    protected Item createItem(Track track, User user) {
        switch (track.getMediaType()) {
            case Audio:
                return createMusicTrack(user, track, ObjectID.PlaylistTrack.getValue() + ";" + encode(track.getId()), ObjectID.Playlist.getValue() + ";" + oidParams);
            case Video:
                return createMovieTrack(user, track, ObjectID.PlaylistTrack.getValue() + ";" + encode(track.getId()), ObjectID.Playlist.getValue() + ";" + oidParams);
            default:
                return null;
        }
    }

}
