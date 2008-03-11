package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.itunes.LibraryListener
 */
public class LibraryListener implements PListHandlerListener {
    private static final Log LOG = LogFactory.getLog(LibraryListener.class);

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
        if ("Library Persistent ID".equals(key)) {
            myLibraryId = (String)value;
        } else if ("Application Version".equals(key)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("iTunes version " + value);
            }
        }
        return true;
    }
}
