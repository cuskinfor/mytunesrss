package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Representation of an album.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AlbumRepresentation implements RestRepresentation {

    private URI myTracksUri;
    private URI myArtistUri;
    private URI myImageUri;
    private URI myM3uUri;
    private URI myXspfUri;
    private URI myRssUri;
    private URI myDownloadUri;
    private String myArtist;
    private Integer myArtistCount;
    private String myImageHash;
    private String myName;
    private String myNaturalSortName;
    private Integer myTrackCount;
    private Integer myYear;

    public AlbumRepresentation() {
    }

    public AlbumRepresentation(Album album) {
        if (IncludeExcludeInterceptor.isAttr("artist")) {
            setArtist(album.getArtist());
        }
        if (IncludeExcludeInterceptor.isAttr("artistCount")) {
            setArtistCount(album.getArtistCount());
        }
        if (IncludeExcludeInterceptor.isAttr("imageHash")) {
            setImageHash(StringUtils.trimToNull(album.getImageHash()));
        }
        if (IncludeExcludeInterceptor.isAttr("name")) {
            setName(album.getName());
        }
        if (IncludeExcludeInterceptor.isAttr("trackCount")) {
            setTrackCount(album.getTrackCount());
        }
        if (IncludeExcludeInterceptor.isAttr("year")) {
            setYear(album.getYear());
        }
        if (IncludeExcludeInterceptor.isAttr("naturalSortName")) {
            setNaturalSortName(album.getNaturalSortName());
        }
    }

    /**
     * URI to the tracks of the album.
     */
    public URI getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(URI tracksUri) {
        myTracksUri = tracksUri;
    }

    /**
     * URI to the album artist.
     */
    public URI getArtistUri() {
        return myArtistUri;
    }

    public void setArtistUri(URI artistUri) {
        myArtistUri = artistUri;
    }

    /**
     * URI to the album image.
     */
    public URI getImageUri() {
        return myImageUri;
    }

    public void setImageUri(URI imageUri) {
        myImageUri = imageUri;
    }

    /**
     * URI for an M3U playlist of the album.
     */
    public URI getM3uUri() {
        return myM3uUri;
    }

    public void setM3uUri(URI m3uUri) {
        myM3uUri = m3uUri;
    }

    /**
     * URI for an XSPF playlist of the album.
     */
    public URI getXspfUri() {
        return myXspfUri;
    }

    public void setXspfUri(URI xspfUri) {
        myXspfUri = xspfUri;
    }

    /**
     * URI for an RSS feed of the album.
     */
    public URI getRssUri() {
        return myRssUri;
    }

    public void setRssUri(URI rssUri) {
        myRssUri = rssUri;
    }

    /**
     * URI to an ZIP archive with all tracks of the album.
     */
    public URI getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(URI downloadUri) {
        myDownloadUri = downloadUri;
    }

    /**
     * Artist of the album.
     */
    public String getArtist() {
        return myArtist;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }

    /**
     * Number of individual artists of the tracks of this album.
     */
    public Integer getArtistCount() {
        return myArtistCount;
    }

    public void setArtistCount(Integer artistCount) {
        myArtistCount = artistCount;
    }

    /**
     * Unique hash for the image of the album.
     */
    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        myImageHash = imageHash;
    }

    /**
     * Name of the album.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * Number of tracks of this album.
     */
    public Integer getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(Integer trackCount) {
        myTrackCount = trackCount;
    }

    /**
     * Year of the album.
     */
    public Integer getYear() {
        return myYear;
    }

    public void setYear(Integer year) {
        myYear = year;
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
