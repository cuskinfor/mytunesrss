/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.xml.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * de.codewave.mytunesrss.datastore.ITunesUtils
 */
public class ITunesUtils {
    private static final Log LOG = LogFactory.getLog(ITunesUtils.class);

    public static File getFileForLocation(String location) {
        try {
            return new File(new URI(location).getPath());
        } catch (URISyntaxException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create URI from location \"" + location + "\".", e);
            }
        }
        return null;
    }

    public static String loadFromITunes(URL iTunesLibraryXml, DataStoreSession storeSession, String previousLibraryId, long timeLastUpdate)
            throws SQLException {
        PListHandler handler = new PListHandler();
        Map<Integer, String> trackIdToPersId = new HashMap<Integer, String>();
        LibraryListener libraryListener = new LibraryListener(previousLibraryId, timeLastUpdate);
        TrackListener trackListener = new TrackListener(storeSession, libraryListener, trackIdToPersId);
        handler.addListener("/plist/dict", libraryListener);
        handler.addListener("/plist/dict", libraryListener);
        handler.addListener("/plist/dict[Tracks]/dict", trackListener);
        handler.addListener("/plist/dict[Playlists]/array", new PlaylistListener(storeSession, trackIdToPersId));
        Set<String> databaseIds = (Set<String>)storeSession.executeQuery(new FindTrackIdsQuery());
        try {
            XmlUtils.parseApplePList(iTunesLibraryXml, handler);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not read data from iTunes xml file.", e);
            }
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Inserted/updated " + trackListener.getUpdatedCount() + " tracks.");
        }
        databaseIds.removeAll(trackListener.getExistingIds());
        if (!databaseIds.isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Removing " + databaseIds.size() + " obsolete tracks.");
            }
            DeleteTrackStatement deleteTrackStatement = new DeleteTrackStatement(storeSession);
            for (String id : databaseIds) {
                deleteTrackStatement.setId(id);
                storeSession.executeStatement(deleteTrackStatement);
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.info(trackListener.getExistingIds().size() + " tracks in the database.");
        }
        return libraryListener.getLibraryId();
    }

    public static class LibraryListener implements PListHandlerListener {
        private String myPreviousLibraryId;
        private String myLibraryId;
        private long myTimeLastUpate;

        public LibraryListener(String previousLibraryId, long timeLastUpate) {
            myPreviousLibraryId = previousLibraryId;
            myTimeLastUpate = timeLastUpate;
        }

        public long getTimeLastUpate() {
            return myTimeLastUpate;
        }

        public String getLibraryId() {
            return myLibraryId;
        }

        public boolean beforeArrayAdd(List array, Object value) {
            throw new UnsupportedOperationException("method beforeArrayAdd of class ITunesUtils$LibraryListener is not implemented!");
        }

        public boolean beforeDictPut(Map dict, String key, Object value) {
            if ("Library Persistent ID".equals(key)) {
                myLibraryId = (String)value;
                if (!value.equals(myPreviousLibraryId)) {
                    myTimeLastUpate = Long.MIN_VALUE;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Library persistent ID changed, updating all tracks regardless of last update time.");
                    }
                }
            } else if ("Application Version".equals(key)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("iTunes version " + value);
                }
            }
            return true;
        }
    }

    public static class TrackListener implements PListHandlerListener {
        private DataStoreSession myDataStoreSession;
        private LibraryListener myLibraryListener;
        private InsertTrackStatement myInsertStatement;
        private UpdateTrackStatement myUpdateStatement;
        private int myUpdatedCount;
        private Set<String> myExistingIds = new HashSet<String>();
        private Set<String> myDatabaseIds = new HashSet<String>();
        private Map<Integer, String> myTrackIdToPersId;

        public TrackListener(DataStoreSession dataStoreSession, LibraryListener libraryListener, Map<Integer, String> trackIdToPersId)
                throws SQLException {
            myDataStoreSession = dataStoreSession;
            myLibraryListener = libraryListener;
            myInsertStatement = new InsertTrackStatement(dataStoreSession);
            myUpdateStatement = new UpdateTrackStatement(dataStoreSession);
            myDatabaseIds = (Set<String>)dataStoreSession.executeQuery(new FindTrackIdsQuery());
            myTrackIdToPersId = trackIdToPersId;
        }

        public Set<String> getExistingIds() {
            return myExistingIds;
        }

        public int getUpdatedCount() {
            return myUpdatedCount;
        }

        public boolean beforeDictPut(Map dict, String key, Object value) {
            Map track = (Map)value;
            String trackId = track.get("Persistent ID") != null ? track.get("Persistent ID").toString() : "TrackID" + track.get("Track ID").toString();
            myExistingIds.add(trackId);
            myTrackIdToPersId.put((Integer)track.get("Track ID"), trackId);
            Date dateModified = ((Date)track.get("Date Modified"));
            long dateModifiedTime = dateModified != null ? dateModified.getTime() : Long.MIN_VALUE;
            Date dateAdded = ((Date)track.get("Date Added"));
            long dateAddedTime = dateAdded != null ? dateAdded.getTime() : Long.MIN_VALUE;
            if (dateModifiedTime >= myLibraryListener.getTimeLastUpate() || dateAddedTime >= myLibraryListener.getTimeLastUpate()) {
                if (insertOrUpdateTrack(track)) {
                    myUpdatedCount++;
                    if (myUpdatedCount % 5000 == 0) { // commit every 5000 tracks to not run out of memory
                        try {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Committing transaction after 5000 inserted/updated tracks.");
                            }
                            myDataStoreSession.commitAndContinue();
                        } catch (SQLException e) {
                            if (LOG.isErrorEnabled()) {
                                LOG.error("Could not commit block of track updates.", e);
                            }
                        }
                    }
                }
            }
            return false;
        }

        public boolean beforeArrayAdd(List array, Object value) {
            throw new UnsupportedOperationException("method beforeArrayAdd of class ITunesUtils$TrackListener is not supported!");
        }

        private boolean insertOrUpdateTrack(Map track) {
            String trackId = track.get("Persistent ID") != null ? track.get("Persistent ID").toString() : "TrackID" + track.get("Track ID").toString();
            String name = (String)track.get("Name");
            String trackType = (String)track.get("Track Type");
            if ("File".equals(trackType)) {
                File file = ITunesUtils.getFileForLocation((String)track.get("Location"));
                if (trackId != null && StringUtils.isNotEmpty(name) && file != null) {
                    try {
                        InsertOrUpdateTrackStatement statement = myDatabaseIds.contains(trackId) ? myUpdateStatement : myInsertStatement;
                        statement.clear();
                        statement.setId(trackId);
                        statement.setName(name.trim());
                        statement.setArtist(StringUtils.trimToNull((String)track.get("Artist")));
                        statement.setAlbum(StringUtils.trimToNull((String)track.get("Album")));
                        statement.setTime(track.get("Total Time") != null ? (Integer)track.get("Total Time") / 1000 : 0);
                        statement.setTrackNumber(track.get("Track Number") != null ? (Integer)track.get("Track Number") : 0);
                        statement.setFileName(file != null ? file.getAbsolutePath() : null);
                        myDataStoreSession.executeStatement(statement);
                        return true;
                    } catch (SQLException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not insert track \"" + name + "\" into database", e);
                        }
                    } catch (NumberFormatException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not insert track \"" + name + "\" into database", e);
                        }
                    }
                }
            }
            return false;
        }
    }

    public static class PlaylistListener implements PListHandlerListener {
        private DataStoreSession myDataStoreSession;
        private Map<Integer, String> myTrackIdToPersId;

        public PlaylistListener(DataStoreSession dataStoreSession, Map<Integer, String> trackIdToPersId) {
            myDataStoreSession = dataStoreSession;
            myTrackIdToPersId = trackIdToPersId;
        }

        public boolean beforeDictPut(Map dict, String key, Object value) {
            throw new UnsupportedOperationException("method beforeDictPut of class ITunesUtils$PlaylistListener is not supported!");
        }

        public boolean beforeArrayAdd(List array, Object value) {
            insertPlaylist((Map)value);
            return false;
        }

        private void insertPlaylist(Map playlist) {
            boolean master = playlist.get("Master") != null && ((Boolean)playlist.get("Master")).booleanValue();
            boolean purchased = playlist.get("Purchased Music") != null && ((Boolean)playlist.get("Purchased Music")).booleanValue();
            boolean partyShuffle = playlist.get("Party Shuffle") != null && ((Boolean)playlist.get("Party Shuffle")).booleanValue();
            boolean podcasts = playlist.get("Podcasts") != null && ((Boolean)playlist.get("Podcasts")).booleanValue();

            if (!master && !purchased && !partyShuffle && !podcasts) {
                Integer id = (Integer)playlist.get("Playlist ID");
                String name = (String)playlist.get("Name");
                List<Map> items = (List<Map>)playlist.get("Playlist Items");
                List<String> tracks = new ArrayList<String>();
                if (items != null && !items.isEmpty()) {
                    for (Iterator<Map> itemIterator = items.iterator(); itemIterator.hasNext();) {
                        Map item = itemIterator.next();
                        tracks.add(myTrackIdToPersId.get((Integer)item.get("Track ID")));
                    }
                }
                if (!tracks.isEmpty()) {
                    SavePlaylistStatement statement = new SaveITunesPlaylistStatement();
                    statement.setId(id.toString());
                    statement.setName(name);
                    statement.setTrackIds(tracks);
                    try {
                        myDataStoreSession.executeStatement(statement);
                    } catch (SQLException e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Could not insert playlist \"" + name + "\" into database.", e);
                        }
                    }
                }
            }
        }
    }
}