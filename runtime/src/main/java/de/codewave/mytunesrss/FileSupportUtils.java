/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * de.codewave.mytunesrss.FileSupportUtils
 */
public class FileSupportUtils {

    public static String getFileSuffix(String filename) {
        int i = filename.lastIndexOf(".");
        if (i != -1 && i + 1 < filename.length()) {
            return filename.substring(i + 1).trim().toLowerCase();
        }
        return null;
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
