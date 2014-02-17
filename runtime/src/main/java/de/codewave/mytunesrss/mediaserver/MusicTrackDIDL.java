/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;

/**
 * Created by mdescher on 17.02.14.
 */
public abstract class MusicTrackDIDL extends TrackDIDL {
    protected Item createMusicTrackItem(Track track, String id, String parentID, User user) {
        MusicTrack item = new MusicTrack(id, parentID, track.getName(), track.getArtist(), track.getAlbum(), track.getArtist(), createTrackResource(track, user));
        item.setDescription(track.getName());
        item.setOriginalTrackNumber(track.getTrackNumber());
        item.setGenres(new String[]{track.getGenre()});
        return item;
    }
}
