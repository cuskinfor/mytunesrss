/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;

public class ArtistAlbumTrackDIDL extends MyTunesRssItemDIDL {

    @Override
    protected Item createItem(Track track, User user) {
        return createMusicTrack(
                user,
                track,
                ObjectID.ArtistAlbumTrack.getValue() + ";" + encode(track.getId()),
                ObjectID.ArtistAlbum.getValue() + ";" + encode(track.getAlbum(), track.getAlbumArtist())
        );
    }

}
