package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.ShutdownRequestedException;
import de.codewave.mytunesrss.datastore.statement.FindPhotoAlbumIdsQuery;
import de.codewave.mytunesrss.datastore.statement.SavePhotoAlbumStatement;
import de.codewave.mytunesrss.datastore.updatequeue.CommitEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreStatementEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.utils.sql.DataStoreSession;
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
public class AlbumListener implements PListHandlerListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumListener.class);

    private static String[] IGNORE_TYPES = new String[] {
            "Selected Event Album",
            "Flagged",
            "Special Roll",
            "Special Month"
    };

    private DatabaseUpdateQueue myQueue;
    protected LibraryListener myLibraryListener;
    private Thread myWatchdogThread;
    private Map<Long, String> myPhotoIdToPersId;
    private Set<String> myPhotoAlbumIds;

    public AlbumListener(Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<Long, String> photoIdToPersId) throws SQLException {
        myPhotoIdToPersId = photoIdToPersId;
        myWatchdogThread = watchdogThread;
        myQueue = queue;
        myLibraryListener = libraryListener;
        myPhotoAlbumIds = new HashSet<String>(MyTunesRss.STORE.executeQuery(new FindPhotoAlbumIdsQuery()));
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        throw new UnsupportedOperationException("method beforeDictPut of class ItunesLoader$PlaylistListener is not supported!");
    }

    public boolean beforeArrayAdd(List array, Object value) {
        insertAlbum((Map) value);
        return false;
    }

    private void insertAlbum(Map album) {
        if (myWatchdogThread.isInterrupted()) {
            throw new ShutdownRequestedException();
        }

        String albumType = (String) album.get("Album Type");
        if (!ArrayUtils.contains(IGNORE_TYPES, albumType)) {
            String albumId = getAlbumId(album);
            if (albumId != null) {
                String albumName = getAlbumName(album);
                List<String> photos = new ArrayList<String>();
                for (String id : (List<String>) album.get("KeyList")) {
                    String persId = myPhotoIdToPersId.get(Long.valueOf(id));
                    if (StringUtils.isNotBlank(persId)) {
                        photos.add(persId);
                    }
                }
                if (!photos.isEmpty()) {
                    SavePhotoAlbumStatement statement = new SavePhotoAlbumStatement();
                    statement.setId(albumId);
                    statement.setName(albumName);
                    statement.setPhotoIds(photos);
                    boolean update = myPhotoAlbumIds.contains(albumId);
                    statement.setUpdate(update);
                    myQueue.offer(new DataStoreStatementEvent(statement, "Could not insert/update photo album \"" + albumName + "\" into database."));
                    if (!update) {
                        myPhotoAlbumIds.add(albumId);
                    }
                    myQueue.offer(new CommitEvent());
                }
            }
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Ignoring album of type \"" + albumType + "\".");
            }

        }
    }

    protected String getAlbumName(Map album) {
        return (String) album.get("AlbumName");
    }

    protected String getAlbumId(Map album) {
        if (myLibraryListener.getLibraryId() == null) {
            return null;
        }
        return myLibraryListener.getLibraryId() + "_" + album.get("AlbumId");
    }

    public Collection<String> getExistingIds() {
        return myPhotoAlbumIds;
    }
}
