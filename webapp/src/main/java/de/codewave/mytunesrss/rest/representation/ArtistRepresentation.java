package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Artist;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ArtistRepresentation extends Artist {
    private Map<String, URI> myUri = new HashMap<String, URI>();

    public ArtistRepresentation(Artist artist) {
        setAlbumCount(artist.getAlbumCount());
        setName(artist.getName());
        setTrackCount(artist.getTrackCount());
    }

    public Map<String, URI> getUri() {
        return myUri;
    }
}
