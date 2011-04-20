/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.mytunesrss.ItunesDatasourceConfig;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.xml.PListHandler;
import de.codewave.utils.xml.XmlUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
 * de.codewave.mytunesrss.datastore.itunes.ItunesLoaderr
 */
public class ItunesLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ItunesLoader.class);

    public static String getFileNameForLocation(String location) {
        if (StringUtils.isNotBlank(location)) {
            try {
                return new File(new URI(location).getPath()).getCanonicalPath();
            } catch (URISyntaxException e) {
                LOG.error("Could not create URI from location \"" + location + "\".", e);
            } catch (IOException e) {
                LOG.debug("Could not create canonical path from location \"" + location + "\".", e);
                try {
                    return MyTunesRssUtils.normalize(new File(new URI(location).getPath()).getAbsolutePath());
                } catch (URISyntaxException e1) {
                    LOG.error("Could not create URI from location \"" + location + "\".", e1);
                }
            }
        }
        return null;
    }

    static File getFileForLocation(String location) {
        try {
            return new File(new URI(location).getPath());
        } catch (URISyntaxException e) {
            LOG.error("Could not create URI from location \"" + location + "\".", e);
        }
        return null;
    }

    /**
     * Load tracks from an iTunes XML file.
     *
     * @param config
     * @param storeSession
     * @param timeLastUpdate
     * @param trackIds
     * @param existingPlaylistIds
     * @return Number of missing files.
     * @throws SQLException
     */
    public static long loadFromITunes(Thread executionThread, ItunesDatasourceConfig config, DataStoreSession storeSession, long timeLastUpdate, Collection<String> trackIds,
                                      Collection<String> existingPlaylistIds) throws SQLException, MalformedURLException {
        TrackListener trackListener = null;
        PlaylistListener playlistListener = null;
        URL iTunesLibraryXml = new File(config.getDefinition()).toURL();
        if (iTunesLibraryXml != null) {
            PListHandler handler = new PListHandler();
            Map<Long, String> trackIdToPersId = new HashMap<Long, String>();
            LibraryListener libraryListener = new LibraryListener(timeLastUpdate);
            trackListener = new TrackListener(config, executionThread, storeSession, libraryListener, trackIdToPersId, trackIds);
            playlistListener = new PlaylistListener(executionThread, storeSession, libraryListener, trackIdToPersId, config);
            handler.addListener("/plist/dict", libraryListener);
            handler.addListener("/plist/dict[Tracks]/dict", trackListener);
            handler.addListener("/plist/dict[Playlists]/array", playlistListener);
            try {
                LOG.info("Parsing iTunes: \"" + iTunesLibraryXml.toString() + "\".");
                XmlUtils.parseApplePList(iTunesLibraryXml, handler);
            } catch (IOException e) {
                LOG.error("Could not read data from iTunes xml file.", e);
            } catch (ParserConfigurationException e) {
                LOG.error("Could not read data from iTunes xml file.", e);
            } catch (SAXException e) {
                LOG.error("Could not read data from iTunes xml file.", e);
            }
            LOG.info("Inserted/updated " + trackListener.getUpdatedCount() + " iTunes tracks. " + trackListener.getMissingFiles() +
                    " files were missing.");
            existingPlaylistIds.removeAll(playlistListener.getExistingIds());
            return trackListener.getMissingFiles();
        }
        return 0;
    }
}