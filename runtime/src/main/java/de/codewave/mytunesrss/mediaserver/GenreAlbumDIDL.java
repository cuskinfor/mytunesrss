/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.datastore.statement.Track;

public class GenreAlbumDIDL extends AlbumDIDL {

    @Override
    protected String getParentId(String album, String artist) {
        return ObjectID.GenreAlbum.getValue() + ";" + encode(album, artist);
    }

    @Override
    protected String getObjectId(Track track) {
        return ObjectID.GenreAlbumTrack.getValue() + ";" + encode(track.getId());
    }

}
