/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.mytunesrss.config.ItunesDatasourceConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.xml.PListHandler;
import de.codewave.utils.xml.XmlUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVStore;
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
import java.util.Date;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.itunes.ItunesLoaderr
 */
public class ItunesLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ItunesLoader.class);
    private static final long MAX_ITUNES_MASTER_XML_DIFF_MILLIS = 1000L * 3600L * 48L; // 48 hours

    public static String getFileNameForLocation(String location) {
        if (StringUtils.isNotBlank(location)) {
            try {
                return new File(new URI(location).getPath()).getCanonicalPath();
            } catch (URISyntaxException e) {
                LOG.error("Could not create URI from location \"" + location + "\".", e);
            } catch (IOException e) {
                LOG.warn("Could not create canonical path from location \"" + location + "\".", e);
                try {
                    return new File(new URI(location).getPath()).getAbsolutePath();
                } catch (URISyntaxException e1) {
                    LOG.error("Could not create URI from location \"" + location + "\".", e1);
                }
            }
        }
        return null;
    }

    /**
     * Load tracks from an iTunes XML file.
     *
     * @param config
     * @param queue
     * @param trackTsUpdate
     * @return Number of missing files.
     * @throws SQLException
     */
    public static MissingItunesFiles loadFromITunes(Thread executionThread, ItunesDatasourceConfig config, DatabaseUpdateQueue queue, Map<String, Long> trackTsUpdate, MVStore mvStore) throws SQLException, MalformedURLException {
        TrackListener trackListener = null;
        PlaylistListener playlistListener = null;
        File iTunesXmlFile = new File(config.getDefinition());
        File iTunesMasterFile = new File(iTunesXmlFile.getParentFile(), "iTunes Library.itl");
        if (!iTunesMasterFile.isFile()) {
            iTunesMasterFile = new File(iTunesXmlFile.getParentFile(), "iTunes Library");
        }
        long iTunesXmlLastModified = iTunesXmlFile.lastModified();
        long iTunesMasterLastModified = iTunesMasterFile.lastModified();
        if (iTunesXmlFile.isFile() && iTunesMasterFile.isFile() && iTunesMasterLastModified - iTunesXmlLastModified > MAX_ITUNES_MASTER_XML_DIFF_MILLIS) {
            LOG.warn("iTunes XML file \"" + iTunesXmlFile.getAbsolutePath() + "\" (last modified at " + new Date(iTunesXmlLastModified) + ") is older than iTunes master file \"" + iTunesMasterFile.getAbsolutePath() + "\" (last modified at " + new Date(iTunesMasterLastModified) + ").");
            MyTunesRss.ADMIN_NOTIFY.notifyOutdatedItunesXml(iTunesMasterFile, iTunesMasterLastModified, iTunesXmlFile, iTunesXmlLastModified);
        }
        URL iTunesLibraryXml = iTunesXmlFile.toURI().toURL();
        PListHandler handler = new PListHandler();
        Map<Long, String> trackIdToPersId = MyTunesRssUtils.openMvMap(mvStore, "trackIdToPers");
        trackIdToPersId.clear();
        LibraryListener libraryListener = new LibraryListener();
        trackListener = new TrackListener(config, executionThread, queue, libraryListener, trackIdToPersId, trackTsUpdate);
        playlistListener = new PlaylistListener(config, executionThread, queue, libraryListener, trackIdToPersId, config);
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
        return new MissingItunesFiles(trackListener.getMissingFiles(), trackListener.getMissingFilePaths());
    }
}
