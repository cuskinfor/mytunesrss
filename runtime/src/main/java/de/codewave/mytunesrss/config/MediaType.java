package de.codewave.mytunesrss.config;

import org.apache.commons.lang3.StringUtils;

/**
 * Media type enumeration.
 */
public enum MediaType {
    Audio("audio"), Video("video"), Image("image"), Other(""), None("");

    public static MediaType get(org.apache.tika.mime.MediaType tikaMediaType) {
        String lowerType = StringUtils.lowerCase(tikaMediaType.getType());
        if ("audio".equals(lowerType)) {
            return Audio;
        } else if ("video".equals(lowerType)) {
            return Video;
        } else if ("image".equals(lowerType)) {
            return Image;
        } else {
            return Other;
        }
    }
    
    private String myMimeTypePrefix;

    private MediaType(String mimeTypePrefix) {
        myMimeTypePrefix = mimeTypePrefix;
    }

    public String getMimeTypePrefix() {
        return myMimeTypePrefix;
    }

    public String toString() {
        return name();
    }
}
