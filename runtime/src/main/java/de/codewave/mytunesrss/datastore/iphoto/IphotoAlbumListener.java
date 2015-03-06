package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.config.PhotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.itunes.PlaylistListenerr
 */
public class IphotoAlbumListener extends AlbumListener implements PListHandlerListener {
    private static String[] IGNORE_TYPES = new String[] {
            "Selected Event Album",
            "Flagged",
            "Special Roll",
            "Special Month"
    };

    public IphotoAlbumListener(PhotoDatasourceConfig datasourceConfig, Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<String, String> photoIdToPersId) throws SQLException {
        super(datasourceConfig, watchdogThread, queue, libraryListener, photoIdToPersId);
    }

    @Override
    protected boolean useAlbum(String albumType) {
        return !ArrayUtils.contains(IGNORE_TYPES, albumType);
    }
}
