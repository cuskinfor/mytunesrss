package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Album;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AlbumRepresentation extends Album {

    private Map<String, URI> myUri = new HashMap<String, URI>();

    public AlbumRepresentation(Album album) {
        setArtist(album.getArtist());
        setArtistCount(album.getArtistCount());
        setImageHash(album.getImageHash());
        setName(album.getName());
        setTrackCount(album.getTrackCount());
        setYear(album.getYear());
    }

    public Map<String, URI> getUri() {
        return myUri;
    }
}
