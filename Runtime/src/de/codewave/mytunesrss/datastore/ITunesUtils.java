/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.datastore.statement.*;import de.codewave.mytunesrss.*;
import de.codewave.utils.xml.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.ITunesUtils
 */
public class ITunesUtils {
    private static final Log LOG = LogFactory.getLog(ITunesUtils.class);

    public static File getFileForLocation(String location) {
        if (location.toLowerCase().startsWith("file://localhost")) {
            try {
                location = location.substring("file://localhost".length());
                location = location.replace("+", "%" + Integer.toHexString('+'));
                String pathname = URLDecoder.decode(location, "UTF-8");
                if (pathname.toLowerCase().endsWith(".mp3") || pathname.toLowerCase().endsWith(".m4a")) {
                    File file = new File(pathname);
                    if (file.exists() && file.isFile()) {
                        return file;
                    } else {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("File \"" + pathname + "\" either not found or not a file.");
                        }
                    }
                } else {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("File [" + location + "] not supported.");
                    }
                }
            } catch (UnsupportedEncodingException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not decode location \"" + location + "\".", e);
                }
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Location \"" + location + "\" not supported.");
            }
        }
        return null;
    }

    public static void loadFromITunes(URL iTunesLibraryXml, DataStoreSession storeSession) throws SQLException {
        try {
            Map plist = (Map)XmlUtils.parseApplePList(iTunesLibraryXml);
            createTracks(plist, storeSession);
            createPlaylists(plist, storeSession);
        } catch (ParserConfigurationException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read data from iTunes xml file.", e);
            }
        } catch (SAXException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read data from iTunes xml file.", e);
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read data from iTunes xml file.", e);
            }
        }
    }

    private static void createTracks(Map plist, DataStoreSession storeSession) throws SQLException {
        Map<String, Map<String, String>> tracks = (Map<String, Map<String, String>>)plist.get("Tracks");
        for (Iterator<Map<String, String>> trackIterator = tracks.values().iterator(); trackIterator.hasNext();) {
            Map<String, String> track = trackIterator.next();
            insertTrack(track, storeSession);
        }
    }

    private static void insertTrack(Map<String, String> track, DataStoreSession storeSession) throws SQLException {
        String trackId = track.get("Track ID");
        String name = track.get("Name");
        File file = ITunesUtils.getFileForLocation(track.get("Location"));
        if (StringUtils.isNotEmpty(trackId) && StringUtils.isNotEmpty(name) && (MyTunesRss.NO_FILE_CHECK || file != null)) {
            try {
                InsertTrackStatement statement = new InsertTrackStatement();
                statement.setId(trackId.trim());
                statement.setName(name.trim());
                statement.setArtist(StringUtils.trimToNull(track.get("Artist")));
                statement.setAlbum(StringUtils.trimToNull(track.get("Album")));
                statement.setTime(StringUtils.isNotEmpty(track.get("Total Time")) ? Integer.parseInt(track.get("Total Time")) / 1000 : 0);
                statement.setTrackNumber(StringUtils.isNotEmpty(track.get("Track Number")) ? Integer.parseInt(track.get("Track Number")) : 0);
                statement.setFileName(file != null ? file.getAbsolutePath() : null);
                storeSession.executeStatement(statement);
            } catch (NumberFormatException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not insert track \"" + name + "\" into database", e);
                }
            }
        }
    }

    private static void createPlaylists(Map plist, DataStoreSession storeSession) throws SQLException {
        List<Map<String, Object>> playlists = (List<Map<String, Object>>)plist.get("Playlists");
        for (Iterator<Map<String, Object>> iterator = playlists.iterator(); iterator.hasNext();) {
            Map<String, Object> playlist = iterator.next();
            if (!Boolean.TRUE.equals(playlist.get("Master"))) {// ignore master list
                insertPlaylist(playlist, storeSession);
            }
        }
    }

    private static void insertPlaylist(Map<String, Object> playlist, DataStoreSession storeSession) throws SQLException {
        String id = (String)playlist.get("Playlist ID");
        String name = (String)playlist.get("Name");

        List<Map<String, String>> items = (List<Map<String, String>>)playlist.get("Playlist Items");
        List<String> tracks = new ArrayList<String>();
        if (items != null && !items.isEmpty()) {
            for (Iterator<Map<String, String>> itemIterator = items.iterator(); itemIterator.hasNext();) {
                Map<String, String> item = itemIterator.next();
                FindTrackQuery findTrackQuery = FindTrackQuery.getForId(new String[] {item.get("Track ID")});
                Collection<Track> trackForId = storeSession.executeQuery(findTrackQuery);
                if (!trackForId.isEmpty()) {
                    tracks.add(trackForId.iterator().next().getId());
                }
            }
        }

        if (!tracks.isEmpty()) {
            InsertPlaylistStatement statement = new InsertITunesPlaylistStatement();
            statement.setId(id);
            statement.setName(name);
            statement.setTrackIds(tracks);
            storeSession.executeStatement(statement);
        }
    }
}