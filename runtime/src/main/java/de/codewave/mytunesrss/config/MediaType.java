package de.codewave.mytunesrss.config;

import org.apache.commons.lang3.StringUtils;

/**
 * Media type enumeration.
 */
public enum MediaType {
    Audio(), Video(), Image(), Other(), None();

    public static MediaType get(String contentType) {
        int i = StringUtils.defaultIfBlank(contentType, "").indexOf('/');
        String lowerType = i > 0 ? StringUtils.lowerCase(contentType.substring(0, i)) : null;
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
    
    public String toString() {
        return name();
    }
}
