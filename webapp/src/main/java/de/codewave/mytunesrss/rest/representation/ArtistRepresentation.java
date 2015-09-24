package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Artist;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of an artist.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ArtistRepresentation implements RestRepresentation {
    /**
     * @exclude from swagger docs
     */
    private String myAlbumsUri;
    /**
     * @exclude from swagger docs
     */
    private String myTracksUri;
    /**
     * @exclude from swagger docs
     */
    private String myM3uUri;
    /**
     * @exclude from swagger docs
     */
    private String myXspfUri;
    /**
     * @exclude from swagger docs
     */
    private String myRssUri;
    /**
     * @exclude from swagger docs
     */
    private String myDownloadUri;
    /**
     * @exclude from swagger docs
     */
    private Integer myAlbumCount;
    /**
     * @exclude from swagger docs
     */
    private String myName;
    /**
     * @exclude from swagger docs
     */
    private String myNaturalSortName;
    /**
     * @exclude from swagger docs
     */
    private Integer myTrackCount;

    public ArtistRepresentation() {
    }

    public ArtistRepresentation(Artist artist) {
        if (IncludeExcludeInterceptor.isAttr("albumCount")) {
            setAlbumCount(artist.getAlbumCount());
        }
        if (IncludeExcludeInterceptor.isAttr("name")) {
            setName(artist.getName());
        }
        if (IncludeExcludeInterceptor.isAttr("trackCount")) {
            setTrackCount(artist.getTrackCount());
        }
        if (IncludeExcludeInterceptor.isAttr("naturalSortName")) {
            setNaturalSortName(artist.getNaturalSortName());
        }
    }

    /**
     * The URI to the list of albums with tracks of this artist.
     */
    public String getAlbumsUri() {
        return myAlbumsUri;
    }

    public void setAlbumsUri(String albumsUri) {
        myAlbumsUri = albumsUri;
    }

    /**
     * The URI to the list of tracks of this artist.
     */
    public String getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(String tracksUri) {
        myTracksUri = tracksUri;
    }

    /**
     * The URI for the M3U playlist with all tracks of this artist.
     */
    public String getM3uUri() {
        return myM3uUri;
    }

    public void setM3uUri(String m3uUri) {
        myM3uUri = m3uUri;
    }

    /**
     * The URI for the XSPF playlist with all tracks of this artist.
     */
    public String getXspfUri() {
        return myXspfUri;
    }

    public void setXspfUri(String xspfUri) {
        myXspfUri = xspfUri;
    }

    /**
     * The URI for the RSS feed with all tracks of this artist.
     */
    public String getRssUri() {
        return myRssUri;
    }

    public void setRssUri(String rssUri) {
        myRssUri = rssUri;
    }

    /**
     * The URI for downloading a ZIP archive with all tracks of this artist.
     */
    public String getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        myDownloadUri = downloadUri;
    }

    /**
     * The number of albums with tracks of this artist.
     */
    public Integer getAlbumCount() {
        return myAlbumCount;
    }

    public void setAlbumCount(Integer albumCount) {
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
    public Integer getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(Integer trackCount) {
        myTrackCount = trackCount;
    }

    /**
     * Natural sort name.
     */
    public String getNaturalSortName() {
        return myNaturalSortName;
    }

    public void setNaturalSortName(String naturalSortName) {
        myNaturalSortName = naturalSortName;
    }
}
