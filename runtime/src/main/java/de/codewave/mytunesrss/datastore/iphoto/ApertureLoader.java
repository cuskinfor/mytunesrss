/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.config.ApertureDatasourceConfig;
import de.codewave.mytunesrss.config.IphotoDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.itunes.IphotoLoaderr
 */
public class ApertureLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ApertureLoader.class);

    /**
     *
     * @param executionThread
     * @param config
     * @param queue
     * @param timeLastUpdate
     * @param photoIds
     * @throws java.sql.SQLException
     * @throws java.net.MalformedURLException
     */
    public static void loadFromAperture(Thread executionThread, ApertureDatasourceConfig config, DatabaseUpdateQueue queue, long timeLastUpdate, Collection<String> photoIds) throws SQLException, MalformedURLException {
        File iPhotoLibraryXmlFile = new File(config.getDefinition(), ApertureDatasourceConfig.APERTURE_XML_FILE_NAME);
        URL iPhotoLibraryXml = iPhotoLibraryXmlFile.toURL();
        if (iPhotoLibraryXml != null) {
            Connection sqliteConnection = null;
            File databaseFile = new File(config.getDefinition(), "Database/Library.apdb");
            if (databaseFile.isFile()) {
                try {
                    Class.forName("org.sqlite.JDBC");
                    sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
                } catch (ClassNotFoundException e) {
                    LOG.warn("Could not use sqlite jdbc driver.", e);
                }
            }
            try {
                PListHandler handler = new PListHandler();
                Map<String, String> photoIdToPersId = new HashMap<String, String>();
                LibraryListener libraryListener = new LibraryListener(iPhotoLibraryXmlFile, timeLastUpdate);
                PhotoListener photoListener = new AperturePhotoListener(config, executionThread, queue, libraryListener, photoIdToPersId, photoIds, sqliteConnection);
                handler.addListener("/plist/dict", libraryListener);
                // first add all photos
                handler.addListener("/plist/dict[Master Image List]/dict", photoListener);
                LOG.info("Parsing Aperture (photos): \"" + iPhotoLibraryXml.toString() + "\".");
                XmlUtils.parseApplePList(iPhotoLibraryXml, handler);
                // then add albums
                handler.removeListener("/plist/dict[Master Image List]/dict");
                IphotoAlbumListener albumListener = null;
                albumListener = new ApertureAlbumListener(executionThread, queue, libraryListener, photoIdToPersId);
                handler.addListener("/plist/dict[List of Albums]/array", albumListener);
                LOG.info("Parsing Aperture (albums): \"" + iPhotoLibraryXml.toString() + "\".");
                XmlUtils.parseApplePList(iPhotoLibraryXml, handler);
                LOG.info("Inserted/updated " + photoListener.getUpdatedCount() + " Aperture photos.");
            } catch (IOException e) {
                LOG.error("Could not read data from Aperture xml file.", e);
            } catch (ParserConfigurationException e) {
                LOG.error("Could not read data from Aperture xml file.", e);
            } catch (SAXException e) {
                LOG.error("Could not read data from Aperture xml file.", e);
            } finally {
                if (sqliteConnection != null) {
                    sqliteConnection.close();
                }
            }
        }
    }
}