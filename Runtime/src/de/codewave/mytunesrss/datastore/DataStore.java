/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.utils.xml.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.DataStore
 */
public class DataStore {
    private static final Log LOG = LogFactory.getLog(DataStore.class);

    static {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not load database driver.", e);
            }
        }
    }

    private TrackResultBuilder myTrackResultBuilder = new TrackResultBuilder();
    private PlaylistResultBuilder myPlaylistResultBuilder = new PlaylistResultBuilder();

    private Connection myConnection;

    private PreparedStatement myInsertTrack;
    private PreparedStatement myInsertPlaylist;
    private PreparedStatement myInsertLinkTrackPlaylist;
    private PreparedStatement myFindTrackById;
    private PreparedStatement myFindTracksByAlbum;
    private PreparedStatement myFindTracksByArtist;
    private PreparedStatement myFindTracks;
    private PreparedStatement myFindAlbumsByArtist;
    private PreparedStatement myFindArtistsByAlbum;
    private PreparedStatement myFindPlaylists;

    public synchronized boolean init() {
        if (myConnection == null) {
            try {
                myConnection = DriverManager.getConnection("jdbc:hsqldb:mem:MyTunesRSS", "sa", "");
                myConnection.createStatement().execute(
                        "CREATE TABLE track ( id varchar, name varchar_ignorecase, artist varchar_ignorecase, album varchar_ignorecase, time integer, track_number integer, file varchar )");
                myConnection.createStatement().execute("CREATE TABLE playlist ( id varchar, name varchar_ignorecase )");
                myConnection.createStatement().execute("CREATE TABLE link_track_playlist ( track_id varchar, playlist_id varchar )");
                myInsertTrack = myConnection.prepareStatement("INSERT INTO track VALUES ( ?, ?, ?, ?, ?, ?, ? )");
                myInsertPlaylist = myConnection.prepareStatement("INSERT INTO playlist VALUES ( ?, ? )");
                myInsertLinkTrackPlaylist = myConnection.prepareStatement("INSERT INTO link_track_playlist VALUES ( ?, ? )");
                myFindTrackById = myConnection.prepareStatement("SELECT id, name, artist, album, time, track_number, file FROM track WHERE id = ?");
                myFindTracks = myConnection.prepareStatement(
                        "SELECT id, name, artist, album, time, track_number, file FROM track WHERE name LIKE ? OR album LIKE ? OR artist LIKE ? ORDER BY album, track_number, name");
                myFindTracksByAlbum = myConnection.prepareStatement(
                        "SELECT id, name, artist, album, time, track_number, file FROM track WHERE album LIKE ? ORDER BY track_number, name");
                myFindTracksByArtist = myConnection.prepareStatement(
                        "SELECT id, name, artist, album, time, track_number, file FROM track WHERE artist LIKE ? ORDER BY album, track_number, name");
                myFindAlbumsByArtist = myConnection.prepareStatement("SELECT DISTINCT(album) FROM track WHERE artist LIKE ? ORDER BY album");
                myFindArtistsByAlbum = myConnection.prepareStatement("SELECT DISTINCT(artist) FROM track WHERE album LIKE ? ORDER BY artist");
                myFindPlaylists = myConnection.prepareStatement(
                        "SELECT p.id AS id, p.name AS name, count(ltp.track_id) AS track_count FROM playlist p, link_track_playlist ltp WHERE ltp.playlist_id = p.id GROUP BY p.id, p.name ORDER BY p.name");
                return true;
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not initialize store.", e);
                }
            }
        }
        return false;
    }

    public synchronized void destroy() {
        if (myConnection != null) {
            try {
                myConnection.close();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not close database connection.", e);
                }
            }
        }
    }

    public synchronized void loadFromITunes(URL iTunesLibraryXml) {
        try {
            Map plist = (Map)XmlUtils.parseApplePList(iTunesLibraryXml);
            createTracks(plist);
            createPlaylists(plist);
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

    private void createTracks(Map plist) {
        Map<String, Map<String, String>> tracks = (Map<String, Map<String, String>>)plist.get("Tracks");
        for (Iterator<Map<String, String>> trackIterator = tracks.values().iterator(); trackIterator.hasNext();) {
            Map<String, String> track = trackIterator.next();
            insertTrack(track);
        }
    }

    private void insertTrack(Map<String, String> track) {
        String trackId = track.get("Track ID");
        String name = track.get("Name");
        File file = TrackUtils.getFileForLocation(track.get("Location"));
        if (StringUtils.isNotEmpty(trackId) && StringUtils.isNotEmpty(name) && file != null) {
            try {
                myInsertTrack.clearParameters();
                myInsertTrack.setString(1, trackId);
                myInsertTrack.setString(2, name);
                myInsertTrack.setString(3, track.get("Artist"));
                myInsertTrack.setString(4, track.get("Album"));
                myInsertTrack.setInt(5, StringUtils.isNotEmpty(track.get("Total Time")) ? Integer.parseInt(track.get("Total Time")) / 1000 : 0);
                myInsertTrack.setInt(6, StringUtils.isNotEmpty(track.get("Track Number")) ? Integer.parseInt(track.get("Track Number")) : 0);
                myInsertTrack.setString(7, file.getAbsolutePath());
                myInsertTrack.executeUpdate();
                commit();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not insert track \"" + name + "\" into database", e);
                }
                rollback();
            } catch (NumberFormatException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not insert track \"" + name + "\" into database", e);
                }
                rollback();
            }
        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Incomplete track not inserted into database.");
            }
        }
    }

    private void createPlaylists(Map plist) {
        List<Map<String, Object>> playlists = (List<Map<String, Object>>)plist.get("Playlists");
        for (Iterator<Map<String, Object>> iterator = playlists.iterator(); iterator.hasNext();) {
            Map<String, Object> playlist = iterator.next();
            if (!Boolean.TRUE.equals(playlist.get("Master"))) {// ignore master list
                insertPlaylist(playlist);
            }
        }
    }

    private void insertPlaylist(Map<String, Object> playlist) {
        String id = (String)playlist.get("Playlist ID");
        String name = (String)playlist.get("Name");
        try {
            myInsertPlaylist.clearParameters();
            myInsertPlaylist.setString(1, id);
            myInsertPlaylist.setString(2, name);
            myInsertPlaylist.executeUpdate();
            List<Map<String, String>> items = (List<Map<String, String>>)playlist.get("Playlist Items");
            if (items != null && !items.isEmpty()) {
                myInsertLinkTrackPlaylist.clearParameters();
                myInsertLinkTrackPlaylist.setString(2, id);
                boolean playlistEmpty = true;
                for (Iterator<Map<String, String>> itemIterator = items.iterator(); itemIterator.hasNext();) {
                    Map<String, String> item = itemIterator.next();
                    if (findTrackById(item.get("Track ID")) != null) {
                        myInsertLinkTrackPlaylist.setString(1, item.get("Track ID"));
                        myInsertLinkTrackPlaylist.executeUpdate();
                        playlistEmpty = false;
                    }
                }
                if (playlistEmpty) {
                    rollback();
                } else {
                    commit();
                }
            } else {
                rollback();
            }
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not insert playlist into database.", e);
            }
            rollback();
        }
    }

    private void commit() {
        try {
            myConnection.commit();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not commit database transaction.", e);
            }
        }
    }

    private void rollback() {
        try {
            myConnection.rollback();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not rollback transaction", e);
            }
        }
    }

    private <T> List<T> executeQuery(PreparedStatement statement, ResultBuilder<T> builder, Object... parameter) {
        try {
            statement.clearParameters();
            if (parameter != null && parameter.length > 0) {
                for (int i = 0; i < parameter.length; i++) {
                    statement.setObject(i + 1, parameter[i]);
                }
            }
            ResultSet resultSet = statement.executeQuery();
            List<T> results = new ArrayList<T>();
            while (resultSet.next()) {
                results.add(builder.create(resultSet));
            }
            return results;
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not execute query.", e);
            }
        }
        return Collections.emptyList();
    }

    private String getWildcardString(String string) {
        return StringUtils.isNotEmpty(string.trim()) ? "%" + string.trim() + "%" : "%";
    }

    public synchronized Track findTrackById(String id) {
        List<Track> results = executeQuery(myFindTrackById, myTrackResultBuilder, id);
        return results != null && results.size() == 1 ? results.get(0) : null;
    }

    public synchronized Collection<Track> findTracksByAlbum(String album) {
        return executeQuery(myFindTracksByAlbum, myTrackResultBuilder, getWildcardString(album));
    }

    public synchronized Collection<Track> findTracksByArtist(String artist) {
        return executeQuery(myFindTracksByArtist, myTrackResultBuilder, getWildcardString(artist));
    }

    public synchronized Collection<Track> findTracks(String search) {
        return executeQuery(myFindTracks, myTrackResultBuilder, getWildcardString(search), getWildcardString(search), getWildcardString(search));
    }

    public synchronized Collection<Track> findAlbumsByArtist(String artist) {
        return executeQuery(myFindAlbumsByArtist, myTrackResultBuilder, getWildcardString(artist));
    }

    public synchronized Collection<String> findArtistsByAlbum(String album) {
        return executeQuery(myFindArtistsByAlbum, new ResultBuilder<String>() {
            public String create(ResultSet resultSet) throws SQLException {
                return resultSet.getString("ARTIST");
            }
        }, getWildcardString(album));
    }

    public synchronized Collection<Playlist> findPlaylists() {
        return executeQuery(myFindPlaylists, myPlaylistResultBuilder);
    }
}