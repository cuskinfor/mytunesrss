/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Photo;

import java.net.URI;

/**
 * Representation of a photo.
 */
public class PhotoRepresentation implements RestRepresentation {
    private String myName;
    private String myFile;
    private long myDate;
    private String myThumbnailImageHash;
    private long myLastThumnailImageUpdate;
    private URI myOriginalImageUri;
    private URI myThumbnailImageUri;
    private URI myExifDataUri;

    public PhotoRepresentation() {
    }

    public PhotoRepresentation(Photo photo) {
        setName(photo.getName());
        setFile(photo.getFile());
        setDate(photo.getDate());
        setThumbnailImageHash(photo.getImageHash());
        setLastThumnailImageUpdate(photo.getLastImageUpdate());
    }

    /**
     * Name of the photo.
     */
    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * File of the photo.
     */
    public String getFile() {
        return myFile;
    }

    public void setFile(String file) {
        myFile = file;
    }

    /**
     * Date of the photo.
     */
    public long getDate() {
        return myDate;
    }

    public void setDate(long date) {
        myDate = date;
    }

    /**
     * Image hash of the thumbnail.
     */
    public String getThumbnailImageHash() {
        return myThumbnailImageHash;
    }

    public void setThumbnailImageHash(String thumbnailImageHash) {
        myThumbnailImageHash = thumbnailImageHash;
    }

    /**
     * Time of the last thumbnail image update.
     */
    public long getLastThumnailImageUpdate() {
        return myLastThumnailImageUpdate;
    }

    public void setLastThumnailImageUpdate(long lastThumnailImageUpdate) {
        myLastThumnailImageUpdate = lastThumnailImageUpdate;
    }

    /**
     * URI to the thumbnail image.
     */
    public URI getThumbnailImageUri() {
        return myThumbnailImageUri;
    }

    public void setThumbnailImageUri(URI thumbnailImageUri) {
        myThumbnailImageUri = thumbnailImageUri;
    }

    /**
     * URI of the original image.
     */
    public URI getOriginalImageUri() {
        return myOriginalImageUri;
    }

    public void setOriginalImageUri(URI originalImageUri) {
        myOriginalImageUri = originalImageUri;
    }

    /**
     * URI to the EXIF data of the photo.
     */
    public URI getExifDataUri() {
        return myExifDataUri;
    }

    public void setExifDataUri(URI exifDataUri) {
        myExifDataUri = exifDataUri;
    }
}
