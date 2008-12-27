/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * de.codewave.mytunesrss.FileSupportUtils
 */
public class FileSupportUtils {
    public static boolean isSupported(String filename) {
        return isSuffixSupported(getFileSuffix(filename));
    }

    private static boolean isSuffixSupported(String suffix) {
        FileType type = MyTunesRss.CONFIG.getFileType(suffix);
        return type != null && type.isActive();
    }

    public static String getFileSuffix(String filename) {
        int i = filename.lastIndexOf(".");
        if (i != -1 && i + 1 < filename.length()) {
            return filename.substring(i + 1).trim().toLowerCase();
        }
        return null;
    }

    public static String getContentType(String filename, boolean forceVideo) {
        FileType type = MyTunesRss.CONFIG.getFileType(getFileSuffix(filename));
        if (type != null) {
            if (forceVideo) {
                return type.getMimeType().replace("audio/", "video/");
            }
            return type.getMimeType();
        }
        return "application/octet-stream";
    }

    public static boolean isProtected(String filename) {
        FileType type = MyTunesRss.CONFIG.getFileType(getFileSuffix(filename));
        return type != null && type.isProtected();
    }

    public static boolean isVideo(String filename) {
        FileType type = MyTunesRss.CONFIG.getFileType(getFileSuffix(filename));
        return type != null && type.isVideo();
    }

    public static boolean isMp3(File file) {
        return file != null && isMp3(file.getName());
    }

    public static boolean isMp3(String filename) {
        return StringUtils.isNotEmpty(filename) && "mp3".equalsIgnoreCase(FilenameUtils.getExtension(filename));
    }

    public static boolean isMp4(File file) {
        return file != null && isMp4(file.getName());
    }

    public static boolean isMp4(String filename) {
        if (StringUtils.isNotEmpty(filename)) {
            String extension = FilenameUtils.getExtension(filename);
            return "mp4".equalsIgnoreCase(extension) || "m4a".equalsIgnoreCase(extension) || "m4v".equalsIgnoreCase(extension) ||
                    "m4p".equalsIgnoreCase(extension);
        }
        return false;
    }
}
