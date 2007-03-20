package de.codewave.mytunesrss;

public enum FileSuffixInfo {

    mp3("mp3", false, false),
    m4a("x-m4a", false, false),
    m4p("x-m4p", false, true),
    wav("wav", false, false),
    mp4("x-mp4", false, false),
    avi("x-msvideo", true, false),
    mov("quicktime", true, false),
    wmv("x-ms-wmv", true, false),
    mpg("mpeg", false, false),
    mpeg("mpeg", false, false),
    flac("application/flac", false, false),
    ogg("application/ogg", false, false),
    m4v("x-m4v", true, false);

    private String myMimeType;
    private boolean myProtected;
    private boolean myVideo;

    FileSuffixInfo(String mimeType, boolean video, boolean protectedType) {
        myMimeType = mimeType;
        myVideo = video;
        myProtected = protectedType;
    }

    public String getMimeType(boolean video) {
        if (myMimeType.contains("/")) {
            return myMimeType;
        }
        return video ? "video/" + myMimeType : "audio/" + myMimeType;
    }

    public boolean isProtected() {
        return myProtected;
    }

    public boolean isVideo() {
        return myVideo;
    }
}
