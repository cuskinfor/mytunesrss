package de.codewave.mytunesrss;

import java.util.ArrayList;
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

    public static List<FileType> getDefaults() {
        List<FileType> types = new ArrayList<FileType>();
        types.add(new FileType(true, "m4a", "audio/x-m4a", false, false));
        types.add(new FileType(true, "m4p", "audio/x-m4p", false, true));
        types.add(new FileType(true, "wav", "audio/wav", false, false));
        types.add(new FileType(true, "mp4", "audio/x-mp4", false, false));
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
        return types;
    }
}