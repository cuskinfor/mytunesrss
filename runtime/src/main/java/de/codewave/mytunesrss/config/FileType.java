package de.codewave.mytunesrss.config;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * de.codewave.mytunesrss.config.FileType
 */
public class FileType {
    private String myMimeType;
    private MediaType myMediaType;
    private boolean myProtected;
    private boolean myActive;
    private String mySuffix;

    public static boolean isValid(FileType fileType) {
        if (StringUtils.isBlank(fileType.getSuffix())) {
            return false;
        }
        if (StringUtils.isBlank(fileType.getMimeType())) {
            return false;
        }
        return true;
    }

    public FileType() {
        // intentionally left blank
    }

    public FileType(FileType other) {
        myActive = other.isActive();
        mySuffix = other.getSuffix();
        myMimeType = other.getMimeType();
        myMediaType = other.getMediaType();
        myProtected = other.isProtected();
    }

    public FileType(boolean active, String suffix, String mimeType, MediaType mediaType, boolean aProtected) {
        myActive = active;
        mySuffix = suffix;
        myMimeType = mimeType;
        myMediaType = mediaType;
        myProtected = aProtected;
    }

    public String getMimeType() {
        if (!StringUtils.contains(myMimeType, "/") && StringUtils.isNotBlank(myMediaType.getMimeTypePrefix())) {
            return myMediaType.getMimeTypePrefix() + "/" + myMimeType;
        }
        return myMimeType;
    }

    public void setMimeType(String mimeType) {
        myMimeType = mimeType;
    }

    public boolean isProtected() {
        return myProtected;
    }

    public void setProtected(boolean aProtected) {
        myProtected = aProtected;
    }

    public MediaType getMediaType() {
        return myMediaType;
    }

    public void setMediaType(MediaType mediaType) {
        myMediaType = mediaType;
    }

    public boolean isActive() {
        return myActive;
    }

    public void setActive(boolean active) {
        myActive = active;
    }

    public String getSuffix() {
        return mySuffix;
    }

    public void setSuffix(String suffix) {
        mySuffix = suffix;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof FileType) {
            return StringUtils.equals(((FileType) obj).getSuffix(), getSuffix());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getSuffix() != null ? getSuffix().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getSuffix() + ": \"" + getMimeType() + "\", active=" + isActive() + ", type=" + myMediaType.name() + ", " +
                (isProtected() ? "protected" : "unprotected");
    }
}
