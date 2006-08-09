/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import java.util.*;

/**
 * de.codewave.mytunesrss.FileSupportUtils
 */
public class FileSupportUtils {
    private static String[] SUFFIXES = new String[] {".mp3", ".m4a", ".m4p", ".wav", ".avi", ".mov", ".wmv", ".mpg", ".mpeg", ".m4v", ".mp4"};
    private static Map<String, String> MIME_TYPES;

    static {
        MIME_TYPES = new HashMap<String, String>(SUFFIXES.length);
        MIME_TYPES.put(".mp3", "/mp3");
        MIME_TYPES.put(".m4a", "/x-m4a");
        MIME_TYPES.put(".m4p", "/x-m4p");
        MIME_TYPES.put(".wav", "/wav");
        MIME_TYPES.put(".mp4", "/x-mp4");
        MIME_TYPES.put(".avi", "/x-msvideo");
        MIME_TYPES.put(".mov", "/quicktime");
        MIME_TYPES.put(".wmv", "/x-ms-wmv");
        MIME_TYPES.put(".mpg", "/mpeg");
        MIME_TYPES.put(".mpeg", "/mpeg");
        MIME_TYPES.put(".m4v", "/x-m4v");
    }

    public static boolean isSupported(String filename) {
        for (String suffix : SUFFIXES) {
            if (filename.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static String getContentType(String filename, boolean video) {
        if (isSupported(filename)) {
            return (video ? "video" : "audio") + MIME_TYPES.get("." + filename.substring(filename.lastIndexOf(".") + 1).toLowerCase());
        }
        return "application/octet-stream";
    }
}
