/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.datastore.statement.Photo;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;

/**
 * Representation of a photo.
 */
public class PhotoRepresentation implements RestRepresentation {
    /**
     * @exclude from swagger docs
     */
    private String myName;
    /**
     * @exclude from swagger docs
     */
    private String myFile;
    /**
     * @exclude from swagger docs
     */
    private Long myDate;
    /**
     * @exclude from swagger docs
     */
    private String myThumbnailImageHash;
    /**
     * @exclude from swagger docs
     */
    private Long myLastThumbnailImageUpdate;
    /**
     * @exclude from swagger docs
     */
    private String myOriginalImageUri;
    /**
     * @exclude from swagger docs
     */
    private String myThumbnailImageUri;
    /**
     * @exclude from swagger docs
     */
    private String myExifDataUri;
    /**
     * @exclude from swagger docs
     */
    private long myWidth;
    /**
     * @exclude from swagger docs
     */
    private long myHeight;

    public PhotoRepresentation() {
    }

    public PhotoRepresentation(Photo photo) {
        if (IncludeExcludeInterceptor.isAttr("name")) {
            setName(photo.getName());
        }
        if (IncludeExcludeInterceptor.isAttr("file")) {
            setFile(photo.getFile());
        }
        if (IncludeExcludeInterceptor.isAttr("date")) {
            setDate(photo.getDate());
        }
        if (IncludeExcludeInterceptor.isAttr("thumbnailImageHash")) {
            setThumbnailImageHash(photo.getImageHash());
        }
        if (IncludeExcludeInterceptor.isAttr("lastThumbnailImageUpdate")) {
            setLastThumbnailImageUpdate(photo.getLastImageUpdate());
        }
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
    public Long getDate() {
        return myDate;
    }

    public void setDate(Long date) {
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
    public Long getLastThumbnailImageUpdate() {
        return myLastThumbnailImageUpdate;
    }

    public void setLastThumbnailImageUpdate(Long lastThumbnailImageUpdate) {
        myLastThumbnailImageUpdate = lastThumbnailImageUpdate;
    }

    /**
     * URI to the thumbnail image.
     */
    public String getThumbnailImageUri() {
        return myThumbnailImageUri;
    }

    public void setThumbnailImageUri(String thumbnailImageUri) {
        myThumbnailImageUri = thumbnailImageUri;
    }

    /**
     * URI of the original image.
     */
    public String getOriginalImageUri() {
        return myOriginalImageUri;
    }

    public void setOriginalImageUri(String originalImageUri) {
        myOriginalImageUri = originalImageUri;
    }

    /**
     * URI to the EXIF data of the photo.
     */
    public String getExifDataUri() {
        return myExifDataUri;
    }

    public void setExifDataUri(String exifDataUri) {
        myExifDataUri = exifDataUri;
    }

    /**
     * Width of the photo in pixels or 0 if unknown for some reason.
     */
    public long getWidth() {
        return myWidth;
    }

    public void setWidth(long width) {
        myWidth = width;
    }

    /**
     * Height of the photo in pixels or 0 if unknown for some reason.
     */
    public long getHeight() {
        return myHeight;
    }

    public void setHeight(long height) {
        myHeight = height;
    }
}
