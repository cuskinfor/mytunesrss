package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of the complete MyTunesRSS library.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class LibraryRepresentation implements RestRepresentation {
    /**
     * @exclude from swagger docs
     */
    private String myAlbumsUri;
    /**
     * @exclude from swagger docs
     */
    private String myArtistsUri;
    /**
     * @exclude from swagger docs
     */
    private String myGenresUri;
    /**
     * @exclude from swagger docs
     */
    private String myMoviesUri;
    /**
     * @exclude from swagger docs
     */
    private String myPlaylistsUri;
    /**
     * @exclude from swagger docs
     */
    private String myTracksUri;
    /**
     * @exclude from swagger docs
     */
    private String myTvShowsUri;
    /**
     * @exclude from swagger docs
     */
    private String myMediaPlayerUri;
    /**
     * @exclude from swagger docs
     */
    private String mySessionUri;
    /**
     * @exclude from swagger docs
     */
    private String myPhotoAlbumsUri;
    /**
     * @exclude from swagger docs
     */
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
    public String getAlbumsUri() {
        return myAlbumsUri;
    }

    public void setAlbumsUri(String albumsUri) {
        myAlbumsUri = albumsUri;
    }

    /**
     * URI to the list of all artists.
     */
    public String getArtistsUri() {
        return myArtistsUri;
    }

    public void setArtistsUri(String artistsUri) {
        myArtistsUri = artistsUri;
    }

    /**
     * URI to the list of all genres.
     */
    public String getGenresUri() {
        return myGenresUri;
    }

    public void setGenresUri(String genresUri) {
        myGenresUri = genresUri;
    }

    /**
     * URI to the list of all movies.
     */
    public String getMoviesUri() {
        return myMoviesUri;
    }

    public void setMoviesUri(String moviesUri) {
        myMoviesUri = moviesUri;
    }

    /**
     * URI to the list of all playlists.
     */
    public String getPlaylistsUri() {
        return myPlaylistsUri;
    }

    public void setPlaylistsUri(String playlistsUri) {
        myPlaylistsUri = playlistsUri;
    }

    /**
     * URI to the list of all tracks.
     */
    public String getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(String tracksUri) {
        myTracksUri = tracksUri;
    }

    /**
     * URI to the list of all TV shows.
     */
    public String getTvShowsUri() {
        return myTvShowsUri;
    }

    public void setTvShowsUri(String tvShowsUri) {
        myTvShowsUri = tvShowsUri;
    }

    /**
     * URI to the media player resource.
     */
    public String getMediaPlayerUri() {
        return myMediaPlayerUri;
    }

    public void setMediaPlayerUri(String mediaPlayerUri) {
        myMediaPlayerUri = mediaPlayerUri;
    }

    /**
     * URI to the session resource.
     */
    public String getSessionUri() {
        return mySessionUri;
    }

    public void setSessionUri(String sessionUri) {
        mySessionUri = sessionUri;
    }

    /**
     * URI to the list of photo albums.
     */
    public String getPhotoAlbumsUri() {
        return myPhotoAlbumsUri;
    }

    public void setPhotoAlbumsUri(String photoAlbumsUri) {
        myPhotoAlbumsUri = photoAlbumsUri;
    }
}
