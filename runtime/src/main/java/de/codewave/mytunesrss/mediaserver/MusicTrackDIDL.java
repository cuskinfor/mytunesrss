/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;

public abstract class MusicTrackDIDL extends TrackDIDL {

    protected Item createMusicTrackItem(Track track, User user) {
        MusicTrack item = new MusicTrack(getObjectId(track), getParentId(track), track.getName(), track.getArtist(), track.getAlbum(), track.getArtist(), createTrackResource(track, user));
        item.setDescription(track.getName());
        item.setOriginalTrackNumber(track.getTrackNumber());
        item.setGenres(new String[]{track.getGenre()});
        return item;
    }

}
