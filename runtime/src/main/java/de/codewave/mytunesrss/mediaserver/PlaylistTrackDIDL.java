/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;

public class PlaylistTrackDIDL extends MyTunesRssItemDIDL {

    @Override
    protected Item createItem(Track track, User user, String oidParams) {
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
