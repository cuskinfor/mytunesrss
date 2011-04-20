/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.IphotoDatasourceConfig;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.xml.PListHandler;
import de.codewave.utils.xml.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
        File iPhotoLibraryXmlFile = new File(config.getDefinition(), IphotoDatasourceConfig.XML_FILE_NAME);
        URL iPhotoLibraryXml = iPhotoLibraryXmlFile.toURL();
        if (iPhotoLibraryXml != null) {
            try {
                PListHandler handler = new PListHandler();
                Map<Long, String> photoIdToPersId = new HashMap<Long, String>();
                LibraryListener libraryListener = new LibraryListener(iPhotoLibraryXmlFile, timeLastUpdate);
                PhotoListener photoListener = new PhotoListener(config, executionThread, storeSession, libraryListener, photoIdToPersId, photoIds);
                handler.addListener("/plist/dict", libraryListener);
                // first add all photos
                handler.addListener("/plist/dict[Master Image List]/dict", photoListener);
                LOG.info("Parsing iPhoto (photos): \"" + iPhotoLibraryXml.toString() + "\".");
                XmlUtils.parseApplePList(iPhotoLibraryXml, handler);
                // then add albums and rolls
                handler.removeListener("/plist/dict[Master Image List]/dict");
                AlbumListener albumListener = null;
                if (config.isImportAlbums()) {
                    albumListener = new AlbumListener(executionThread, storeSession, libraryListener, photoIdToPersId);
                    handler.addListener("/plist/dict[List of Albums]/array", albumListener);
                }
                RollListener rollListener = null;
                if (config.isImportRolls()) {
                    rollListener = new RollListener(executionThread, storeSession, libraryListener, photoIdToPersId);
                    handler.addListener("/plist/dict[List of Rolls]/array", rollListener);
                }
                LOG.info("Parsing iPhoto (albums/rolls): \"" + iPhotoLibraryXml.toString() + "\".");
                XmlUtils.parseApplePList(iPhotoLibraryXml, handler);
                LOG.info("Inserted/updated " + photoListener.getUpdatedCount() + " iPhoto photos.");
                if (albumListener != null) {
                    existsingAlbumIds.removeAll(albumListener.getExistingIds());
                }
                if (rollListener != null) {
                    existsingAlbumIds.removeAll(rollListener.getExistingIds());
                }
            } catch (IOException e) {
                LOG.error("Could not read data from iTunes xml file.", e);
            } catch (ParserConfigurationException e) {
                LOG.error("Could not read data from iTunes xml file.", e);
            } catch (SAXException e) {
                LOG.error("Could not read data from iTunes xml file.", e);
            }
        }
    }
}