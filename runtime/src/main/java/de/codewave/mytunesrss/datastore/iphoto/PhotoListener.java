/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.config.CompiledReplacementRule;
import de.codewave.mytunesrss.config.PhotoDatasourceConfig;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.mytunesrss.datastore.statement.HandlePhotoImagesStatement;
import de.codewave.mytunesrss.datastore.statement.InsertOrUpdatePhotoStatement;
import de.codewave.mytunesrss.datastore.statement.InsertPhotoStatement;
import de.codewave.mytunesrss.datastore.statement.UpdatePhotoStatement;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreStatementEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.mytunesrss.meta.MyTunesRssExifUtils;
import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.TrackListenerr
 */
public abstract class PhotoListener implements PListHandlerListener {

    private DatabaseUpdateQueue myQueue;
    private LibraryListener myLibraryListener;
    private int myUpdatedCount;
    private Map<String, String> myPhotoIdToPersId;
    private Collection<String> myPhotoIds;
    private Thread myWatchdogThread;
    private Set<CompiledReplacementRule> myPathReplacements;
    private PhotoDatasourceConfig myDatasourceConfig;
    protected long myXmlModDate;

    public PhotoListener(PhotoDatasourceConfig datasourceConfig, Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<String, String> photoIdToPersId,
                         Collection<String> photoIds) throws SQLException {
        myDatasourceConfig = datasourceConfig;
        myWatchdogThread = watchdogThread;
        myQueue = queue;
        myLibraryListener = libraryListener;
        myPhotoIdToPersId = photoIdToPersId;
        myPhotoIds = photoIds;
        myPathReplacements = new HashSet<CompiledReplacementRule>();
        for (ReplacementRule pathReplacement : myDatasourceConfig.getPathReplacements()) {
            myPathReplacements.add(new CompiledReplacementRule(pathReplacement));
        }
    }

    public int getUpdatedCount() {
        return myUpdatedCount;
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        Map photo = (Map) value;
        String photoId = calculatePhotoId(key, photo);
        if (photoId != null) {
            try {
                if (processPhoto(key, photo, photoId, myPhotoIds.remove(photoId))) {
                    myUpdatedCount++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    private String calculatePhotoId(String key, Map photo) {
        if (myLibraryListener.getLibraryId() == null) {
            return null;
        }
        String photoId = myLibraryListener.getLibraryId() + "_";
        photoId += photo.get("GUID") != null ? MyTunesRssBase64Utils.encode((String) photo.get("GUID")) : "PhotoID" + key;
        return photoId;
    }

    public boolean beforeArrayAdd(List array, Object value) {
        throw new UnsupportedOperationException("method beforeArrayAdd of iPhoto photo listener is not supported!");
    }

    private boolean processPhoto(String key, Map photo, String photoId, boolean existing) throws InterruptedException {
        if (myWatchdogThread.isInterrupted()) {
            Thread.currentThread().interrupt();
            throw new ShutdownRequestedException();
        }
        String name = (String) photo.get("Caption");
        String mediaType = (String) photo.get("MediaType");
        if ("Image".equals(mediaType)) {
            String filename = applyReplacements(getImagePath(photo));
            if (StringUtils.isNotBlank(filename) && myDatasourceConfig.isSupported(filename)) {
                File file = MyTunesRssUtils.searchFile(filename);
                if (file.isFile() && (!existing || myXmlModDate >= myLibraryListener.getTimeLastUpate() || file.lastModified() >= myLibraryListener.getTimeLastUpate())) {
                    InsertOrUpdatePhotoStatement statement = existing ? new UpdatePhotoStatement(myDatasourceConfig.getId()) : new InsertPhotoStatement(myDatasourceConfig.getId());
                    statement.clear();
                    statement.setId(photoId);
                    statement.setName(MyTunesRssUtils.compose(name.trim()));
                    Double dateAsTimerInterval = (Double) photo.get("DateAsTimerInterval");
                    Double modDateAsTimerInterval = (Double) photo.get("ModDateAsTimerInterval");
                    Long createDate = null;
                    // preference order for date:
                    // 1) date from xml
                    // 2) exif date
                    // 3) modification date from xml
                    if (dateAsTimerInterval != null) {
                        createDate = Long.valueOf((dateAsTimerInterval.longValue() * 1000L) + 978303600000L);
                    } else {
                        createDate = MyTunesRssExifUtils.getCreateDate(file);
                        if (createDate == null && modDateAsTimerInterval != null) {
                            createDate = Long.valueOf((modDateAsTimerInterval.longValue() * 1000L) + 978303600000L);
                        }
                    }
                    statement.setDate(createDate);
                    statement.setFile(filename);
                    myQueue.offer(new DataStoreStatementEvent(statement, true, "Could not insert photo \"" + name + "\" into database"));
                    //HandlePhotoImagesStatement handlePhotoImagesStatement = new HandlePhotoImagesStatement(file, photoId, 0);
                    //myQueue.offer(new DataStoreStatementEvent(handlePhotoImagesStatement, false, "Could not insert photo \"" + name + "\" into database"));
                    myPhotoIdToPersId.put(key, photoId);
                    return true;
                } else if (existing) {
                    myPhotoIdToPersId.put(key, photoId);
                }
                return false;
            }
        }
        return false;
    }

    private String getImagePath(Map photo) {
        return (String) photo.get("ImagePath");
    }

    private String applyReplacements(String originalFileName) {
        for (CompiledReplacementRule pathReplacement : myPathReplacements) {
            if (pathReplacement.matches(originalFileName)) {
                return pathReplacement.replace(originalFileName);
            }
        }
        return originalFileName;
    }
}
