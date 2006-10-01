package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.utils.xml.*;
import org.apache.commons.logging.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.LibraryListener
 */
public class LibraryListener implements PListHandlerListener {
    private static final Log LOG = LogFactory.getLog(LibraryListener.class);

    private String myPreviousLibraryId;
    private String myLibraryId;
    private long myTimeLastUpate;

    public LibraryListener(String previousLibraryId, long timeLastUpate) {
        myPreviousLibraryId = previousLibraryId;
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
        if ("Library Persistent ID".equals(key)) {
            myLibraryId = (String)value;
            if (!value.equals(myPreviousLibraryId)) {
                myTimeLastUpate = Long.MIN_VALUE;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Library persistent ID changed, updating all tracks regardless of last update time.");
                }
            }
        } else if ("Application Version".equals(key)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("iTunes version " + value);
            }
        }
        return true;
    }
}
