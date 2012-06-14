package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Album;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class AlbumRepresentation {

    private URI myTracksUri;
    private URI myArtistUri;
    private URI myImageUri;
    private URI myM3uUri;
    private URI myXspfUri;
    private URI myRssUri;
    private URI myDownloadUri;
    private URI myTagsUri;
    private String myArtist;
    private int myArtistCount;
    private String myImageHash;
    private String myName;
    private int myTrackCount;
    private int myYear;

    public AlbumRepresentation() {
    }

    public AlbumRepresentation(Album album) {
        setArtist(album.getArtist());
        setArtistCount(album.getArtistCount());
        setImageHash(album.getImageHash());
        setName(album.getName());
        setTrackCount(album.getTrackCount());
        setYear(album.getYear());
    }

    public URI getTracksUri() {
        return myTracksUri;
    }

    public void setTracksUri(URI tracksUri) {
        myTracksUri = tracksUri;
    }

    public URI getArtistUri() {
        return myArtistUri;
    }

    public void setArtistUri(URI artistUri) {
        myArtistUri = artistUri;
    }

    public URI getImageUri() {
        return myImageUri;
    }

    public void setImageUri(URI imageUri) {
        myImageUri = imageUri;
    }

    public URI getM3uUri() {
        return myM3uUri;
    }

    public void setM3uUri(URI m3uUri) {
        myM3uUri = m3uUri;
    }

    public URI getXspfUri() {
        return myXspfUri;
    }

    public void setXspfUri(URI xspfUri) {
        myXspfUri = xspfUri;
    }

    public URI getRssUri() {
        return myRssUri;
    }

    public void setRssUri(URI rssUri) {
        myRssUri = rssUri;
    }

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

    public String getArtist() {
        return myArtist;
    }

    public void setArtist(String artist) {
        myArtist = artist;
    }

    public int getArtistCount() {
        return myArtistCount;
    }

    public void setArtistCount(int artistCount) {
        myArtistCount = artistCount;
    }

    public String getImageHash() {
        return myImageHash;
    }

    public void setImageHash(String imageHash) {
        myImageHash = imageHash;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public int getTrackCount() {
        return myTrackCount;
    }

    public void setTrackCount(int trackCount) {
        myTrackCount = trackCount;
    }

    public int getYear() {
        return myYear;
    }

    public void setYear(int year) {
        myYear = year;
    }
}
