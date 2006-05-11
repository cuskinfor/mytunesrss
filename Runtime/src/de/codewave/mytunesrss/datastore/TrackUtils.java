/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;

/**
 * de.codewave.mytunesrss.datastore.TrackUtils
 */
public class TrackUtils {
    private static final Log LOG = LogFactory.getLog(TrackUtils.class);

    public static File getFileForLocation(String location) {
        if (location.toLowerCase().startsWith("file://localhost")) {
            try {
                location = location.substring("file://localhost".length());
                location = location.replace("+", "%" + Integer.toHexString('+'));
                String pathname = URLDecoder.decode(location, "UTF-8");
                if (pathname.toLowerCase().endsWith(".mp3") || pathname.toLowerCase().endsWith(".m4a")) {
                    File file = new File(pathname);
                    if (file.exists() && file.isFile()) {
                        return file;
                    } else {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("File \"" + pathname + "\" either not found or not a file.");
                        }
                    }
                } else {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("File [" + location + "] not supported.");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not decode location \"" + location + "\".", e);
                }
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Location \"" + location + "\" not supported.");
            }
        }
        return null;
    }
}