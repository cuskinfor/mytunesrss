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
    private boolean myVideo;
    private boolean myProtected;
    private boolean myActive;
    private String mySuffix;

    public static List<FileType> getDefaults() {
        List<FileType> types = new ArrayList<FileType>();
        types.add(new FileType(true, "m4a", "audio/x-m4a", false, false));
        types.add(new FileType(true, "m4p", "audio/x-m4p", false, true));
        types.add(new FileType(true, "wav", "audio/wav", false, false));
        types.add(new FileType(true, "mp4", "video/x-mp4", false, false));
        types.add(new FileType(true, "avi", "video/x-msvideo", true, false));
        types.add(new FileType(true, "mov", "video/quicktime", true, false));
        types.add(new FileType(true, "wmv", "video/x-ms-wmv", true, false));
        types.add(new FileType(true, "wma", "audio/x-ms-wma", false, false));
        types.add(new FileType(true, "mpg", "audio/mpeg", false, false));
        types.add(new FileType(true, "mpeg", "audio/mpeg", false, false));
        types.add(new FileType(true, "flac", "application/flac", false, false));
        types.add(new FileType(true, "ogg", "application/ogg", false, false));
        types.add(new FileType(true, "m4v", "video/x-m4v", true, false));
        types.add(new FileType(true, "m4b", "audio/x-m4b", false, false));
        types.add(new FileType(true, "mp3", "audio/mp3", false, false));
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
        myVideo = other.isVideo();
        myProtected = other.isProtected();
    }

    public FileType(boolean active, String suffix, String mimeType, boolean video, boolean aProtected) {
        myActive = active;
        mySuffix = suffix;
        myMimeType = mimeType;
        myVideo = video;
        myProtected = aProtected;
    }

    public String getMimeType() {
        if (myMimeType.contains("/")) {
            return myMimeType;
        }
        return (myVideo ? "video" : "audio") + "/" + myMimeType;
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

    public boolean isVideo() {
        return myVideo;
    }

    public void setVideo(boolean video) {
        myVideo = video;
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
            return StringUtils.equals(((FileType)obj).getSuffix(), getSuffix());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getSuffix() != null ? getSuffix().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getSuffix() + ": \"" + getMimeType() + "\", active=" + isActive() + ", type=" + (isVideo() ? "video" : "audio") + ", " +
                (isProtected() ? "protected" : "unprotected");
    }
}