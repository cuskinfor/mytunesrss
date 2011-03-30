/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.Base64Utils;
import de.codewave.utils.xml.PListHandlerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.iphoto.LibraryListener
 */
public class LibraryListener implements PListHandlerListener {
    private static final Logger LOG = LoggerFactory.getLogger(LibraryListener.class);

    private String myLibraryId;
    private long myTimeLastUpate;

    public LibraryListener(long timeLastUpate) {
        myTimeLastUpate = timeLastUpate;
    }

    public long getTimeLastUpate() {
        return myTimeLastUpate;
    }

    public String getLibraryId() {
        return myLibraryId;
    }

    public boolean beforeArrayAdd(List array, Object value) {
        throw new UnsupportedOperationException("method beforeArrayAdd of class ItunesLoader$LibraryListener is not implemented!");
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        if ("Archive Path".equals(key)) {
            myLibraryId = Base64Utils.encode(MyTunesRss.SHA1_DIGEST.digest(MyTunesRssUtils.getUtf8Bytes((String) value)));
        } else if ("Application Version".equals(key)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("iPhoto version " + value);
            }
        }
        return true;
    }
}
