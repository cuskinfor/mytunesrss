/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.lang.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.FileSupportUtils
 */
public class FileSupportUtils {
    private static Set<String> SUPPORTED_SUFFIXES = new HashSet<String>();

    static {
        for (FileSuffixInfo fileSuffixInfo : FileSuffixInfo.values()) {
            SUPPORTED_SUFFIXES.add(fileSuffixInfo.name());
        }
    }

    public static boolean isSupported(String filename) {
        if (SUPPORTED_SUFFIXES.contains(getFileSuffix(filename))) {
            if (StringUtils.isEmpty(MyTunesRss.CONFIG.getFileTypes())) {
                return true;
            }
            for (StringTokenizer tokenizer = new StringTokenizer(MyTunesRss.CONFIG.getFileTypes().toLowerCase(), ","); tokenizer.hasMoreTokens();) {
                if (filename.toLowerCase().endsWith("." + tokenizer.nextToken())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSuffixSupported(String suffix) {
        if (SUPPORTED_SUFFIXES.contains(suffix)) {
            if (StringUtils.isEmpty(MyTunesRss.CONFIG.getFileTypes())) {
                return true;
            }
            for (StringTokenizer tokenizer = new StringTokenizer(MyTunesRss.CONFIG.getFileTypes().toLowerCase(), ","); tokenizer.hasMoreTokens();) {
                if (suffix.equalsIgnoreCase(tokenizer.nextToken())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static FileSuffixInfo getFileSuffixInfo(String filename) {
        String suffix = getFileSuffix(filename);
        if (isSuffixSupported(suffix)) {
            return FileSuffixInfo.valueOf(suffix);
        }
        return null;
    }

    public static String getFileSuffix(String filename) {
        int i = filename.lastIndexOf(".");
        if (i != -1 && i + 1 < filename.length()) {
            return filename.substring(i + 1).trim().toLowerCase();
        }
        return null;
    }

    public static String getContentType(String filename, boolean video) {
        FileSuffixInfo fileSuffixInfo = getFileSuffixInfo(filename);
        if (fileSuffixInfo != null) {
            return fileSuffixInfo.getMimeType(video);
        }
        return "application/octet-stream";
    }

    public static boolean isProtected(String filename) {
        FileSuffixInfo fileSuffixInfo = getFileSuffixInfo(filename);
        return fileSuffixInfo != null && fileSuffixInfo.isProtected();
    }

    public static boolean isVideo(String filename) {
        FileSuffixInfo fileSuffixInfo = getFileSuffixInfo(filename);
        return fileSuffixInfo != null && fileSuffixInfo.isVideo();
    }
}
