/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.xml.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
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

    public static void loadFromITunes(URL iTunesLibraryXml, DataStoreSession storeSession, long timeLastUpdate) throws SQLException {
        try {
            PListHandler handler = new PListHandler();
            TrackListener trackListener = new TrackListener(storeSession, timeLastUpdate);
            handler.addListener("/plist/dict[Tracks]/dict", trackListener);
            handler.addListener("/plist/dict[Playlists]/array", new PlaylistListener(storeSession));
            Set<String> databaseIds = (Set<String>)storeSession.executeQuery(new FindTrackIdsQuery());
            XmlUtils.parseApplePList(iTunesLibraryXml, handler);
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

    public static class TrackListener implements PListHandlerListener {
        DataStoreSession myDataStoreSession;
        long myTimeLastUpdate;
        InsertTrackStatement myInsertStatement;
        UpdateTrackStatement myUpdateStatement;
        int myUpdatedCount;
        Set<String> myExistingIds = new HashSet<String>();

        public TrackListener(DataStoreSession dataStoreSession, long timeLastUpdate) {
            myDataStoreSession = dataStoreSession;
            myTimeLastUpdate = timeLastUpdate;
            myInsertStatement = new InsertTrackStatement(dataStoreSession);
            myUpdateStatement = new UpdateTrackStatement(dataStoreSession);
        }

        public Set<String> getExistingIds() {
            return myExistingIds;
        }

        public int getUpdatedCount() {
            return myUpdatedCount;
        }

        public boolean beforeDictPut(Map dict, String key, Object value) {
            Map track = (Map)value;
            myExistingIds.add(track.get("Track ID").toString());
            Date dateModified = ((Date)track.get("Date Modified"));
            long dateModifiedTime = dateModified != null ? dateModified.getTime() : Long.MIN_VALUE;
            Date dateAdded = ((Date)track.get("Date Added"));
            long dateAddedTime = dateAdded != null ? dateAdded.getTime() : Long.MIN_VALUE;
            if (dateModifiedTime >= myTimeLastUpdate || dateAddedTime >= myTimeLastUpdate) {
                if (insertOrUpdateTrack(track)) {
                    myUpdatedCount++;
                }
            }
            return false;
        }

        public boolean beforeArrayAdd(List array, Object value) {
            throw new UnsupportedOperationException("method beforeArrayAdd of class ITunesUtils$TrackListener is not supported!");
        }

        private boolean insertOrUpdateTrack(Map track) {
            String trackId = track.get("Track ID").toString();
            String name = (String)track.get("Name");
            String trackType = (String)track.get("Track Type");
            if ("File".equals(trackType)) {
                File file = ITunesUtils.getFileForLocation((String)track.get("Location"));
                if (trackId != null && StringUtils.isNotEmpty(name) && file != null) {
                    try {
                        Collection<Track> tracks = myDataStoreSession.executeQuery(FindTrackQuery.getForId(new String[] {trackId}));
                        boolean exists = tracks != null && !tracks.isEmpty();
                        InsertOrUpdateTrackStatement statement = exists ? myUpdateStatement : myInsertStatement;
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
        DataStoreSession myDataStoreSession;

        public PlaylistListener(DataStoreSession dataStoreSession) {
            myDataStoreSession = dataStoreSession;
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
                        tracks.add(item.get("Track ID").toString());
                    }
                }
                if (!tracks.isEmpty()) {
                    InsertPlaylistStatement statement = new InsertITunesPlaylistStatement();
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