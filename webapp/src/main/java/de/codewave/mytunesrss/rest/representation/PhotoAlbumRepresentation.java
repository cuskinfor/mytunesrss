/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.PhotoAlbum;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a photo album.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PhotoAlbumRepresentation implements RestRepresentation {

    /**
     * @exclude from swagger docs
     */
    private long myFirstDate;
    /**
     * @exclude from swagger docs
     */
    private long myLastDate;
    /**
     * @exclude from swagger docs
     */
    private String myName;
    /**
     * @exclude from swagger docs
     */
    private int myPhotoCount;
    /**
     * @exclude from swagger docs
     */
    private String myPhotosUri;

    public PhotoAlbumRepresentation() {
    }

    public PhotoAlbumRepresentation(PhotoAlbum photoAlbum) {
        if (IncludeExcludeInterceptor.isAttr("firstDate")) {
            setFirstDate(photoAlbum.getFirstDate());
        }
        if (IncludeExcludeInterceptor.isAttr("lastDate")) {
            setLastDate(photoAlbum.getLastDate());
        }
        if (IncludeExcludeInterceptor.isAttr("name")) {
            setName(photoAlbum.getName());
        }
        if (IncludeExcludeInterceptor.isAttr("photoCount")) {
            setPhotoCount(photoAlbum.getPhotoCount());
        }
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
    public String getPhotosUri() {
        return myPhotosUri;
    }

    public void setPhotosUri(String photosUri) {
        myPhotosUri = photosUri;
    }
}
