/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.xml.PListHandler;
import de.codewave.utils.xml.XmlUtils;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.iphoto.IphotoLoader
 */
public class IphotoLoader {
    private static final Logger LOG = LoggerFactory.getLogger(IphotoLoader.class);

    /**
     *
     * @param executionThread
     * @param config
     * @param queue
     * @param photoTsUpdate
     * @throws SQLException
     * @throws MalformedURLException
     */
    public static void loadFromIPhoto(Thread executionThread, IphotoDatasourceConfig config, DatabaseUpdateQueue queue, Map<String, Long> photoTsUpdate, Map<String, String> photoSourceId, MVStore mvStore) throws SQLException, MalformedURLException {
        File iPhotoLibraryXmlFile = new File(config.getDefinition(), IphotoDatasourceConfig.IPHOTO_XML_FILE_NAME);
        URL iPhotoLibraryXml = iPhotoLibraryXmlFile.toURI().toURL();
        if (iPhotoLibraryXml != null) {
            try {
                PListHandler handler = new PListHandler();
                Map<String, String> photoIdToPersId = MyTunesRssUtils.openMvMap(mvStore, "trackIdToPers");
                photoIdToPersId.clear();
                LibraryListener libraryListener = new LibraryListener(iPhotoLibraryXmlFile);
                PhotoListener photoListener = new IphotoPhotoListener(config, executionThread, queue, libraryListener, photoIdToPersId, photoTsUpdate, photoSourceId);
                handler.addListener("/plist/dict", libraryListener);
                // first add all photos
                handler.addListener("/plist/dict[Master Image List]/dict", photoListener);
                LOG.info("Parsing iPhoto (photos): \"" + iPhotoLibraryXml + "\".");
                XmlUtils.parseApplePList(iPhotoLibraryXml, handler);
                // then add albums and rolls
                handler.removeListener("/plist/dict[Master Image List]/dict");
                IphotoAlbumListener albumListener = null;
                if (config.isImportAlbums()) {
                    albumListener = new IphotoAlbumListener(config, executionThread, queue, libraryListener, photoIdToPersId);
                    handler.addListener("/plist/dict[List of Albums]/array", albumListener);
                }
                RollListener rollListener = null;
                if (config.isImportRolls()) {
                    rollListener = new RollListener(config, executionThread, queue, libraryListener, photoIdToPersId);
                    handler.addListener("/plist/dict[List of Rolls]/array", rollListener);
                }
                LOG.info("Parsing iPhoto (albums/rolls): \"" + iPhotoLibraryXml + "\".");
                XmlUtils.parseApplePList(iPhotoLibraryXml, handler);
                LOG.info("Inserted/updated " + photoListener.getUpdatedCount() + " iPhoto photos.");
            } catch (IOException | SAXException | ParserConfigurationException e) {
                LOG.error("Could not read data from iPhoto xml file.", e);
            }
        }
    }

}
