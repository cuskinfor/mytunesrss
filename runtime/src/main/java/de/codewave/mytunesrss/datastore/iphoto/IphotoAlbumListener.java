package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.ShutdownRequestedException;
import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.config.PhotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.statement.FindPhotoAlbumIdsQuery;
import de.codewave.mytunesrss.datastore.statement.SavePhotoAlbumStatement;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreStatementEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

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

    protected boolean useAlbum(String albumType) {
        return !ArrayUtils.contains(IGNORE_TYPES, albumType);
    }
}
