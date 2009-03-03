package de.codewave.mytunesrss;

/**
 * Media type enumeration.
 */
public enum MediaType {
    Audio("audio"), Video("video"), Image("image"), Other("");

    private String myMimeTypePrefix;

    private MediaType(String mimeTypePrefix) {
        myMimeTypePrefix = mimeTypePrefix;
    }

    public String getMimeTypePrefix() {
        return myMimeTypePrefix;
    }

    public String getJspName() {
        return name();
    }
}
