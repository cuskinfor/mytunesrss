package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.ShutdownRequestedException;
import de.codewave.mytunesrss.datastore.statement.FindPhotoAlbumIdsQuery;
import de.codewave.mytunesrss.datastore.statement.SavePhotoAlbumStatement;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.PlaylistListenerr
 */
public class AlbumListener implements PListHandlerListener {
    private static final Logger LOG = LoggerFactory.getLogger(AlbumListener.class);

    private DataStoreSession myDataStoreSession;
    private Set<String> myExistingIds = new HashSet<String>();
    protected LibraryListener myLibraryListener;
    private Thread myWatchdogThread;
    private Map<Long, String> myPhotoIdToPersId;

    public AlbumListener(Thread watchdogThread, DataStoreSession dataStoreSession, LibraryListener libraryListener, Map<Long, String> photoIdToPersId) {
        myPhotoIdToPersId = photoIdToPersId;
        myWatchdogThread = watchdogThread;
        myDataStoreSession = dataStoreSession;
        myLibraryListener = libraryListener;
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
                try {
                    statement.setUpdate(myDataStoreSession.executeQuery(new FindPhotoAlbumIdsQuery()).contains(albumId));
                    myDataStoreSession.executeStatement(statement);
                    myExistingIds.add(albumId);
                    DatabaseBuilderCallable.doCheckpoint(myDataStoreSession, true);
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not insert/update photo album \"" + albumName + "\" into database.", e);
                    }
                }
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
        return myExistingIds;
    }
}
