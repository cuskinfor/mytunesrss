/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import java.util.*;

/**
 * de.codewave.mytunesrss.FileSupportUtils
 */
public class FileSupportUtils {
    private static String[] AUDIO_SUFFIXES = new String[] {".mp3", ".m4a", ".m4p", ".wav"};
    private static String[] VIDEO_SUFFIXES = new String[] {".avi", ".mov", ".wmv", ".mpg", ".mpeg", ".m4v"};
    private static Map<String, String> MIME_TYPES;

    static {
        MIME_TYPES = new HashMap<String, String>(AUDIO_SUFFIXES.length + VIDEO_SUFFIXES.length);
        // audio types
        MIME_TYPES.put(".mp3", "audio/mp3");
        MIME_TYPES.put(".m4a", "audio/x-m4a");
        MIME_TYPES.put(".m4p", "audio/x-m4p");
        MIME_TYPES.put(".wav", "audio/wav");
        // video types
        MIME_TYPES.put(".avi", "video/x-msvideo");
        MIME_TYPES.put(".mov", "video/quicktime");
        MIME_TYPES.put(".wmv", "video/x-ms-wmv");
        MIME_TYPES.put(".mpg", "video/mpeg");
        MIME_TYPES.put(".mpeg", "video/mpeg");
        MIME_TYPES.put(".m4v", "video/x-m4v");
    }

    public static boolean isSupported(String filename) {
        return isSupportedAudio(filename) || isSupportedVideo(filename);
    }

    public static boolean isSupportedAudio(String filename) {
        for (String suffix : AUDIO_SUFFIXES) {
            if (filename.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportedVideo(String filename) {
        for (String suffix : VIDEO_SUFFIXES) {
            if (filename.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    public static String getContentType(String filename) {
        if (isSupported(filename)) {
            return MIME_TYPES.get(filename.substring(filename.lastIndexOf(".") + 1).toLowerCase());
        }
        return "application/octet-stream";
    }
}
