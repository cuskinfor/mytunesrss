package de.codewave.mytunesrss;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * de.codewave.mytunesrss.FileType
 */
public class FileType {
    private String myMimeType;
    private MediaType myMediaType;
    private boolean myProtected;
    private boolean myActive;
    private String mySuffix;

    public static List<FileType> getDefaults() {
        List<FileType> types = new ArrayList<FileType>();
        types.add(new FileType(true, "m4a", "audio/x-m4a", MediaType.Audio, false));
        types.add(new FileType(true, "m4p", "audio/x-m4p", MediaType.Audio, true));
        types.add(new FileType(true, "wav", "audio/wav", MediaType.Audio, false));
        types.add(new FileType(true, "mp4", "video/x-mp4", MediaType.Video, false));
        types.add(new FileType(true, "avi", "video/x-msvideo", MediaType.Video, false));
        types.add(new FileType(true, "mov", "video/quicktime", MediaType.Video, false));
        types.add(new FileType(true, "wmv", "video/x-ms-wmv", MediaType.Video, false));
        types.add(new FileType(true, "wma", "audio/x-ms-wma", MediaType.Audio, false));
        types.add(new FileType(true, "mpg", "audio/mpeg", MediaType.Audio, false));
        types.add(new FileType(true, "mpeg", "audio/mpeg", MediaType.Audio, false));
        types.add(new FileType(true, "flac", "application/flac", MediaType.Audio, false));
        types.add(new FileType(true, "ogg", "application/ogg", MediaType.Audio, false));
        types.add(new FileType(true, "m4v", "video/x-m4v", MediaType.Video, false));
        types.add(new FileType(true, "m4b", "audio/x-m4b", MediaType.Audio, false));
        types.add(new FileType(true, "mp3", "audio/mp3", MediaType.Audio, false));
        types.add(new FileType(true, "jpg", "image/jpeg", MediaType.Image, false));
        types.add(new FileType(true, "gif", "image/gif", MediaType.Image, false));
        types.add(new FileType(true, "png", "image/png", MediaType.Image, false));
        Collections.sort(types, new Comparator<FileType>() {
            public int compare(FileType o1, FileType o2) {
                return o1.getSuffix().compareTo(o2.getSuffix());
            }
        });
        return types;
    }

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