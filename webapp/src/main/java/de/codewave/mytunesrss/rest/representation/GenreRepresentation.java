/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Genre;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class GenreRepresentation extends Genre {

    private Map<String, URI> myUri = new HashMap<String, URI>();

    public GenreRepresentation(Genre genre) {
        setAlbumCount(genre.getAlbumCount());
        setArtistCount(genre.getArtistCount());
        setHidden(genre.isHidden());
        setName(genre.getName());
        setTrackCount(genre.getTrackCount());
    }

    public Map<String, URI> getUri() {
        return myUri;
    }
}
