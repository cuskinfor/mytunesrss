/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.ShutdownRequestedException;
import de.codewave.mytunesrss.config.PhotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.statement.FindPhotoAlbumIdsQuery;
import de.codewave.mytunesrss.datastore.statement.SavePhotoAlbumStatement;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreStatementEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.PlaylistListenerr
 */
public abstract class AlbumListener implements PListHandlerListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlbumListener.class);

    private DatabaseUpdateQueue myQueue;
    protected LibraryListener myLibraryListener;
    private Thread myWatchdogThread;
    private Map<String, String> myPhotoIdToPersId;
    private Set<String> myPhotoAlbumIds;
    private PhotoDatasourceConfig myDatasourceConfig;

    protected AlbumListener(PhotoDatasourceConfig datasourceConfig, Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<String, String> photoIdToPersId) throws SQLException {
        myDatasourceConfig = datasourceConfig;
        myPhotoIdToPersId = photoIdToPersId;
        myWatchdogThread = watchdogThread;
        myQueue = queue;
        myLibraryListener = libraryListener;
        myPhotoAlbumIds = new HashSet<>(MyTunesRss.STORE.executeQuery(new FindPhotoAlbumIdsQuery()));
    }

    @Override
    public boolean beforeDictPut(Map dict, String key, Object value) {
        throw new UnsupportedOperationException("method beforeDictPut of photo album listener is not supported!");
    }

    @Override
    public boolean beforeArrayAdd(List array, Object value) {
        try {
            insertAlbum((Map) value);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private void insertAlbum(Map album) throws InterruptedException {
        if (myWatchdogThread.isInterrupted()) {
            Thread.currentThread().interrupt();
            throw new ShutdownRequestedException();
        }

        String albumType = (String) album.get("Album Type");
        if (useAlbum(albumType)) {
            String albumId = getAlbumId(album);
            if (albumId != null) {
                String albumName = getAlbumName(album);
                List<String> photos = new ArrayList<>();
                for (String id : (List<String>) album.get("KeyList")) {
                    String persId = myPhotoIdToPersId.get(id);
                    if (StringUtils.isNotBlank(persId)) {
                        photos.add(persId);
                    }
                }
                if (!photos.isEmpty()) {
                    SavePhotoAlbumStatement statement = new SavePhotoAlbumStatement(myDatasourceConfig.getId());
                    statement.setId(albumId);
                    statement.setName(albumName);
                    statement.setPhotoIds(photos);
                    boolean update = myPhotoAlbumIds.contains(albumId);
                    statement.setUpdate(update);
                    myQueue.offer(new DataStoreStatementEvent(statement, true, "Could not insert/update photo album \"" + albumName + "\" into database."));
                    if (!update) {
                        myPhotoAlbumIds.add(albumId);
                    }
                }
            }
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Ignoring album of type \"" + albumType + "\".");
            }

        }
    }

    protected abstract boolean useAlbum(String albumType);

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
