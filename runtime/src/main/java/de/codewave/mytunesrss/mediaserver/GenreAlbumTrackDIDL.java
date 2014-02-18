/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;

public class GenreAlbumTrackDIDL extends MyTunesRssItemDIDL {

    @Override
    protected Item createItem(Track track, User user) {
        return createMusicTrack(user, 
                track,
                ObjectID.GenreAlbumTrack.getValue() + ";" + encode(track.getId()),
                ObjectID.GenreAlbum.getValue() + ";" + encode(track.getAlbum(), track.getAlbumArtist())
        );
    }

}
