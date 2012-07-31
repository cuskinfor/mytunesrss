/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.PhotoAlbum;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * Representation of a photo album.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PhotoAlbumRepresentation {

    private long myFirstDate;
    private long myLastDate;
    private String myName;
    private int myPhotoCount;
    private URI myPhotosUri;

    public PhotoAlbumRepresentation() {
    }

    public PhotoAlbumRepresentation(PhotoAlbum photoAlbum) {
        setFirstDate(photoAlbum.getFirstDate());
        setLastDate(photoAlbum.getLastDate());
        setName(photoAlbum.getName());
        setPhotoCount(photoAlbum.getPhotoCount());
    }

    /**
     * Earliest date of all photos in the album in milliseconds since January 1, 1970.
     */
    public long getFirstDate() {
        return myFirstDate;
    }

    public void setFirstDate(long firstDate) {
        myFirstDate = firstDate;
    }

    /**
     * Latest date of all photos in the album in milliseconds since January 1, 1970.
     */
    public long getLastDate() {
        return myLastDate;
    }

    public void setLastDate(long lastDate) {
        myLastDate = lastDate;
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
     * Number of photos in the album.
     */
    public int getPhotoCount() {
        return myPhotoCount;
    }

    public void setPhotoCount(int photoCount) {
        myPhotoCount = photoCount;
    }

    /**
     * URI to the photos of the album.
     */
    public URI getPhotosUri() {
        return myPhotosUri;
    }

    public void setPhotosUri(URI photosUri) {
        myPhotosUri = photosUri;
    }
}
