/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.camel.mp4.Mp4Utils;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.itunes.ItunesLoader;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.TrackListenerr
 */
public class PhotoListener implements PListHandlerListener {
    private static final Logger LOG = LoggerFactory.getLogger(PhotoListener.class);

    private DataStoreSession myDataStoreSession;
    private LibraryListener myLibraryListener;
    private int myUpdatedCount;
    private Map<Long, String> myPhotoIdToPersId;
    private Collection<String> myPhotoIds;
    private Thread myWatchdogThread;
    private Set<CompiledPathReplacement> myPathReplacements;
    private IphotoDatasourceConfig myDatasourceConfig;

    public PhotoListener(IphotoDatasourceConfig datasourceConfig, Thread watchdogThread, DataStoreSession dataStoreSession, LibraryListener libraryListener, Map<Long, String> photoIdToPersId,
                         Collection<String> photoIds) throws SQLException {
        myDatasourceConfig = datasourceConfig;
        myWatchdogThread = watchdogThread;
        myDataStoreSession = dataStoreSession;
        myLibraryListener = libraryListener;
        myPhotoIdToPersId = photoIdToPersId;
        myPhotoIds = photoIds;
        myPathReplacements = new HashSet<CompiledPathReplacement>();
        for (PathReplacement pathReplacement : myDatasourceConfig.getPathReplacements()) {
            myPathReplacements.add(new CompiledPathReplacement(pathReplacement));
        }
    }

    public int getUpdatedCount() {
        return myUpdatedCount;
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        Map photo = (Map) value;
        String photoId = calculatePhotoId(key, photo);
        myPhotoIdToPersId.put(Long.valueOf(key), photoId);
        if (processPhoto(key, photo, myPhotoIds.remove(photoId))) {
            myUpdatedCount++;
            DatabaseBuilderCallable.updateHelpTables(myDataStoreSession, myUpdatedCount);
        }
        DatabaseBuilderCallable.doCheckpoint(myDataStoreSession, false);
        return false;
    }

    private String calculatePhotoId(String key, Map photo) {
        String photoId = myLibraryListener.getLibraryId() + "_";
        photoId += photo.get("GUID") != null ? photo.get("GUID").toString() : "PhotoID" + key;
        return photoId;
    }

    public boolean beforeArrayAdd(List array, Object value) {
        throw new UnsupportedOperationException("method beforeArrayAdd of class ItunesLoader$TrackListener is not supported!");
    }

    private boolean processPhoto(String key, Map photo, boolean existing) {
        if (myWatchdogThread.isInterrupted()) {
            throw new ShutdownRequestedException();
        }
        String photoId = calculatePhotoId(key, photo);
        String name = (String) photo.get("Caption");
        String mediaType = (String) photo.get("Media Type");
        if ("Image".equals(mediaType)) {
            String filename = applyReplacements(ItunesLoader.getFileNameForLocation((String) photo.get("ImagePath")));
            if (StringUtils.isNotBlank(filename)) {
                long date = getTimestamp((Float) photo.get("DateAsTimerInterval"));
                long modDate = getTimestamp((Float) photo.get("ModDateAsTimerInterval"));
                long metaModDate = getTimestamp((Float) photo.get("MetaModDateAsTimerInterval"));
                if (!existing || modDate >= myLibraryListener.getTimeLastUpate() || metaModDate >= myLibraryListener.getTimeLastUpate()) {
                    try {
                        InsertOrUpdatePhotoStatement statement = existing ? new UpdatePhotoStatement() : new InsertPhotoStatement();
                        statement.clear();
                        statement.setId(photoId);
                        statement.setName(MyTunesRssUtils.normalize(name.trim()));
                        statement.setDate(date);
                        myDataStoreSession.executeStatement(statement);
                        return true;
                    } catch (SQLException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not insert photo \"" + name + "\" into database", e);
                        }
                    }
                }
                return false;
            }
        }
        myPhotoIdToPersId.remove(key);
        return false;
    }

    private long getTimestamp(Float timerInterval) {
        if (timerInterval == null) {
            return 0;
        }
        
    }

    private String applyReplacements(String originalFileName) {
        for (CompiledPathReplacement pathReplacement : myPathReplacements) {
            if (pathReplacement.matches(originalFileName)) {
                return pathReplacement.replace(originalFileName);
            }
        }
        return originalFileName;
    }
}
