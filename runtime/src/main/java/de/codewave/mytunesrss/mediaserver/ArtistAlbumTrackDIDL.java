/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;

public class ArtistAlbumTrackDIDL extends MusicTrackDIDL {

    @Override
    protected Item createTrackItem(Track track, String id, String parentID, User user) {
        return createMusicTrackItem(track, id, parentID, user);
    }

    @Override
    protected String getParentId(Track track) {
        return ObjectID.ArtistAlbum.getValue() + ";" + encode(track.getAlbum(), track.getAlbumArtist());
    }

    @Override
    protected String getObjectId(Track track) {
        return ObjectID.ArtistAlbumTrack.getValue() + ";" + encode(track.getId());
    }

}
