/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;

public class GenreAlbumTrackDIDL extends MusicTrackDIDL {

    @Override
    protected Item createTrackItem(Track track, String id, String parentID, User user) {
        return createMusicTrackItem(track, id, parentID, user);
    }

    @Override
    protected String getParentId(Track track) {
        return ObjectID.GenreAlbum.getValue() + ";" + encode(track.getAlbum(), track.getAlbumArtist());
    }

    @Override
    protected String getObjectId(Track track) {
        return ObjectID.GenreAlbumTrack.getValue() + ";" + encode(track.getId());
    }

}
