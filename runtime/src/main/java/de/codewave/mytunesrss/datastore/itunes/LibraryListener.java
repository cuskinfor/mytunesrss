package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.utils.xml.PListHandlerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.itunes.LibraryListener
 */
public class LibraryListener implements PListHandlerListener {
    private static final Logger LOG = LoggerFactory.getLogger(LibraryListener.class);

    private String myLibraryId;

    public String getLibraryId() {
        return myLibraryId;
    }

    @Override
    public boolean beforeArrayAdd(List array, Object value) {
        throw new UnsupportedOperationException("method beforeArrayAdd of iTunes library listener is not implemented!");
    }

    @Override
    public boolean beforeDictPut(Map dict, String key, Object value) {
        if ("Library Persistent ID".equals(key)) {
            myLibraryId = (String) value;
        } else if ("Application Version".equals(key)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("iTunes version " + value);
            }
        }
        return true;
    }
}
