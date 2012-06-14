package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Artist;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of an artist.
 */
@XmlRootElement
public class ArtistRepresentation {
    private URI myAlbumsUri;
    private URI myM3uUri;
    private URI myXspfUri;
    private URI myRssUri;
    private URI myDownloadUri;
    private URI myTagsUri;
    private int myAlbumCount;
    private String myName;
    private int myTrackCount;

    public ArtistRepresentation() {
    }

    public ArtistRepresentation(Artist artist) {
        setAlbumCount(artist.getAlbumCount());
        setName(artist.getName());
        setTrackCount(artist.getTrackCount());
    }

    /**
     * The URI to the list of albums with tracks of this artist.
     */
    public URI getAlbumsUri() {
        return myAlbumsUri;
    }

    public void setAlbumsUri(URI albumsUri) {
        myAlbumsUri = albumsUri;
    }

    /**
     * The URI for the M3U playlist with all tracks of this artist.
     */
    public URI getM3uUri() {
        return myM3uUri;
    }

    public void setM3uUri(URI m3uUri) {
        myM3uUri = m3uUri;
    }

    /**
     * The URI for the XSPF playlist with all tracks of this artist.
     */
    public URI getXspfUri() {
        return myXspfUri;
    }

    public void setXspfUri(URI xspfUri) {
        myXspfUri = xspfUri;
    }

    /**
     * The URI for the RSS feed with all tracks of this artist.
     */
    public URI getRssUri() {
        return myRssUri;
    }

    public void setRssUri(URI rssUri) {
        myRssUri = rssUri;
    }

    /**
     * The URI for downloading a ZIP archive with all tracks of this artist.
     */
    public URI getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(URI downloadUri) {
        myDownloadUri = downloadUri;
    }

    public URI getTagsUri() {
        return myTagsUri;
    }

    public void setTagsUri(URI tagsUri) {
        myTagsUri = tagsUri;
    }

    /**
     * The number of albums with tracks of this artist.
     */
    public int getAlbumCount() {
        return myAlbumCount;
    }

    public void setAlbumCount(int albumCount) {
        myAlbumCount = albumCount;
    }

    /**
     * The artist name.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * The number of tracks of this artist.
     */
    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }
}