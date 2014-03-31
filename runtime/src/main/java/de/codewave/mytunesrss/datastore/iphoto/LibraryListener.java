/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.utils.io.IOUtils;
import de.codewave.utils.xml.PListHandlerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.iphoto.LibraryListener
 */
public class LibraryListener implements PListHandlerListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryListener.class);

    private String myLibraryId;
    private File myIphotoLibraryXml;

    public LibraryListener(File iphotoLibraryXml) {
        myIphotoLibraryXml = iphotoLibraryXml;
    }

    public String getLibraryId() {
        return myLibraryId;
    }

    public boolean beforeArrayAdd(List array, Object value) {
        throw new UnsupportedOperationException("method beforeArrayAdd of iPhoto library listener is not implemented!");
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        if ("Archive Path".equals(key)) {
            try {
                myLibraryId = IOUtils.getFilenameHash(myIphotoLibraryXml);
            } catch (IOException ignored) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not create library id for file \"" + myIphotoLibraryXml + "\". No files from this library will be imported.");
                }
            }
        } else if ("Application Version".equals(key)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("iPhoto version " + value);
            }
        }
        return true;
    }
}
