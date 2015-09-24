package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Album;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of an album.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class AlbumRepresentation implements RestRepresentation {

    /**
     * @exclude from swagger docs
     */
    private String myTracksUri;
    /**
     * @exclude from swagger docs
     */
    private String myArtistUri;
    /**
     * @exclude from swagger docs
     */
    private String myImageUri;
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
    private String myArtist;
    /**
     * @exclude from swagger docs
     */
    private Integer myArtistCount;
    /**
     * @exclude from swagger docs
     */
    private String myImageHash;
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
    /**
     * @exclude from swagger docs
     */
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
    public String getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(String tracksUri) {
        myTracksUri = tracksUri;
    }

    /**
     * URI to the album artist.
     */
    public String getArtistUri() {
        return myArtistUri;
    }

    public void setArtistUri(String artistUri) {
        myArtistUri = artistUri;
    }

    /**
     * URI to the album image.
     */
    public String getImageUri() {
        return myImageUri;
    }

    public void setImageUri(String imageUri) {
        myImageUri = imageUri;
    }

    /**
     * URI for an M3U playlist of the album.
     */
    public String getM3uUri() {
        return myM3uUri;
    }

    public void setM3uUri(String m3uUri) {
        myM3uUri = m3uUri;
    }

    /**
     * URI for an XSPF playlist of the album.
     */
    public String getXspfUri() {
        return myXspfUri;
    }

    public void setXspfUri(String xspfUri) {
        myXspfUri = xspfUri;
    }

    /**
     * URI for an RSS feed of the album.
     */
    public String getRssUri() {
        return myRssUri;
    }

    public void setRssUri(String rssUri) {
        myRssUri = rssUri;
    }

    /**
     * URI to an ZIP archive with all tracks of the album.
     */
    public String getDownloadUri() {
        return myDownloadUri;
    }

    public void setDownloadUri(String downloadUri) {
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
