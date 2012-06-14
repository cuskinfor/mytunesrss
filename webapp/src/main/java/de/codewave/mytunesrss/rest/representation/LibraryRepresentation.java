package de.codewave.mytunesrss.rest.representation;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of the complete MyTunesRSS library.
 */
@XmlRootElement
public class LibraryRepresentation {
    private URI myAlbumsUri;
    private URI myArtistsUri;
    private URI myGenresUri;
    private URI myMoviesUri;
    private URI myPlaylistsUri;
    private URI myTracksUri;
    private URI myTvShowsUri;
    private String myVersion;

    /**
     * Version of the MyTunesRSS server instance.
     */
    public String getVersion() {
        return myVersion;
    }

    public void setVersion(String version) {
        myVersion = version;
    }

    public URI getAlbumsUri() {
        return myAlbumsUri;
    }

    public void setAlbumsUri(URI albumsUri) {
        myAlbumsUri = albumsUri;
    }

    public URI getArtistsUri() {
        return myArtistsUri;
    }

    public void setArtistsUri(URI artistsUri) {
        myArtistsUri = artistsUri;
    }

    public URI getGenresUri() {
        return myGenresUri;
    }

    public void setGenresUri(URI genresUri) {
        myGenresUri = genresUri;
    }

    public URI getMoviesUri() {
        return myMoviesUri;
    }

    public void setMoviesUri(URI moviesUri) {
        myMoviesUri = moviesUri;
    }

    public URI getPlaylistsUri() {
        return myPlaylistsUri;
    }

    public void setPlaylistsUri(URI playlistsUri) {
        myPlaylistsUri = playlistsUri;
    }

    public URI getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(URI tracksUri) {
        myTracksUri = tracksUri;
    }

    public URI getTvShowsUri() {
        return myTvShowsUri;
    }

    public void setTvShowsUri(URI tvShowsUri) {
        myTvShowsUri = tvShowsUri;
    }
}
