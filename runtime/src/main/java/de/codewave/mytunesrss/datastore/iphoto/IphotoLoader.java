/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.IphotoDatasourceConfig;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.xml.PListHandler;
import de.codewave.utils.xml.XmlUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.itunes.IphotoLoaderr
 */
public class IphotoLoader {
    private static final Logger LOG = LoggerFactory.getLogger(IphotoLoader.class);

    /**
     * Load tracks from an iPhoto XML file.
     *
     * @param config
     * @param storeSession
     * @param timeLastUpdate
     * @param photoIds
     * @return Number of missing files.
     * @throws java.sql.SQLException
     */
    public static void loadFromIPhoto(Thread executionThread, IphotoDatasourceConfig config, DataStoreSession storeSession, long timeLastUpdate, Collection<String> photoIds, Collection<String> existsingAlbumIds) throws SQLException, MalformedURLException {
        URL iPhotoLibraryXml = new File(config.getDefinition(), IphotoDatasourceConfig.XML_FILE_NAME).toURL();
        if (iPhotoLibraryXml != null) {
            PListHandler handler = new PListHandler();
            Map<Long, String> photoIdToPersId = new HashMap<Long, String>();
            LibraryListener libraryListener = new LibraryListener(timeLastUpdate);
            PhotoListener photoListener = new PhotoListener(config, executionThread, storeSession, libraryListener, photoIdToPersId, photoIds);
            AlbumListener albumListener = new AlbumListener(executionThread, storeSession, libraryListener, photoIdToPersId, config);
            RollListener rollListener = new RollListener(executionThread, storeSession, libraryListener, photoIdToPersId, config);
            handler.addListener("/plist/dict", libraryListener);
            handler.addListener("/plist/dict[Master Image List]/dict", photoListener);
            //handler.addListener("/plist/dict[List of Albums]/array/dict", albumListener);
            //handler.addListener("/plist/dict[List of Rools]/array/dict", rollListener);
            try {
                LOG.info("Parsing iPhoto: \"" + iPhotoLibraryXml.toString() + "\".");
                XmlUtils.parseApplePList(iPhotoLibraryXml, handler);
            } catch (Exception e) {
                LOG.error("Could not read data from iPhoto xml file.", e);
            }
            LOG.info("Inserted/updated " + photoListener.getUpdatedCount() + " iPhoto photos.");
            existsingAlbumIds.removeAll(albumListener.getExistingIds());
        }
    }
}