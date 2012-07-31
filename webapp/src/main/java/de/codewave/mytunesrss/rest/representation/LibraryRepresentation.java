package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Representation of the complete MyTunesRSS library.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LibraryRepresentation implements RestRepresentation {
    private URI myAlbumsUri;
    private URI myArtistsUri;
    private URI myGenresUri;
    private URI myMoviesUri;
    private URI myPlaylistsUri;
    private URI myTracksUri;
    private URI myTvShowsUri;
    private URI myMediaPlayerUri;
    private URI mySessionUri;
    private URI myPhotoAlbumsUri;
    private VersionRepresentation myVersion;

    /**
     * Version of the MyTunesRSS server instance.
     */
    public VersionRepresentation getVersion() {
        return myVersion;
    }

    public void setVersion(VersionRepresentation version) {
        myVersion = version;
    }

    /**
     * URI to the list of all albums.
     */
    public URI getAlbumsUri() {
        return myAlbumsUri;
    }

    public void setAlbumsUri(URI albumsUri) {
        myAlbumsUri = albumsUri;
    }

    /**
     * URI to the list of all artists.
     */
    public URI getArtistsUri() {
        return myArtistsUri;
    }

    public void setArtistsUri(URI artistsUri) {
        myArtistsUri = artistsUri;
    }

    /**
     * URI to the list of all genres.
     */
    public URI getGenresUri() {
        return myGenresUri;
    }

    public void setGenresUri(URI genresUri) {
        myGenresUri = genresUri;
    }

    /**
     * URI to the list of all movies.
     */
    public URI getMoviesUri() {
        return myMoviesUri;
    }

    public void setMoviesUri(URI moviesUri) {
        myMoviesUri = moviesUri;
    }

    /**
     * URI to the list of all playlists.
     */
    public URI getPlaylistsUri() {
        return myPlaylistsUri;
    }

    public void setPlaylistsUri(URI playlistsUri) {
        myPlaylistsUri = playlistsUri;
    }

    /**
     * URI to the list of all tracks.
     */
    public URI getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(URI tracksUri) {
        myTracksUri = tracksUri;
    }

    /**
     * URI to the list of all TV shows.
     */
    public URI getTvShowsUri() {
        return myTvShowsUri;
    }

    public void setTvShowsUri(URI tvShowsUri) {
        myTvShowsUri = tvShowsUri;
    }

    /**
     * URI to the media player resource.
     */
    public URI getMediaPlayerUri() {
        return myMediaPlayerUri;
    }

    public void setMediaPlayerUri(URI mediaPlayerUri) {
        myMediaPlayerUri = mediaPlayerUri;
    }

    /**
     * URI to the session resource.
     */
    public URI getSessionUri() {
        return mySessionUri;
    }

    public void setSessionUri(URI sessionUri) {
        mySessionUri = sessionUri;
    }

    /**
     * URI to the list of photo albums.
     */
    public URI getPhotoAlbumsUri() {
        return myPhotoAlbumsUri;
    }

    public void setPhotoAlbumsUri(URI photoAlbumsUri) {
        myPhotoAlbumsUri = photoAlbumsUri;
    }
}
