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

/**
 * de.codewave.mytunesrss.datastore.ITunesUtils
 */
public class ITunesUtils {
    private static final Log LOG = LogFactory.getLog(ITunesUtils.class);

    public static File getFileForLocation(String location) {
        try {
            String pathname = URLEncoder.encode(new URI(location).getPath(), "UTF-8");
            return new File(pathname);
        } catch (UnsupportedEncodingException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not encode location \"" + location + "\".", e);
            }
        } catch (URISyntaxException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create URI from location \"" + location + "\".", e);
            }
        }
        return null;
    }

    public static void loadFromITunes(URL iTunesLibraryXml, DataStoreSession storeSession) throws SQLException {
        try {
            PListHandler handler = new PListHandler();
            handler.addListener("/plist/dict[Tracks]/dict", new TrackListener(storeSession));
            handler.addListener("/plist/dict[Playlists]/array", new PlaylistListener(storeSession));
            XmlUtils.parseApplePList(iTunesLibraryXml, handler);
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

        public TrackListener(DataStoreSession dataStoreSession) {
            myDataStoreSession = dataStoreSession;
        }

        public boolean beforeDictPut(Map dict, String key, Object value) {
            insertTrack((Map)value);
            return false;
        }

        public boolean beforeArrayAdd(List array, Object value) {
            throw new UnsupportedOperationException("method beforeArrayAdd of class ITunesUtils$TrackListener is not supported!");
        }

        private void insertTrack(Map track) {
            Integer trackId = (Integer)track.get("Track ID");
            String name = (String)track.get("Name");
            String trackType = (String)track.get("Track Type");
            if ("File".equals(trackType)) {
                File file = ITunesUtils.getFileForLocation((String)track.get("Location"));
                if (trackId != null && StringUtils.isNotEmpty(name) && file != null) {
                    try {
                        InsertTrackStatement statement = new InsertTrackStatement();
                        statement.setId(trackId.toString());
                        statement.setName(name.trim());
                        statement.setArtist(StringUtils.trimToNull((String)track.get("Artist")));
                        statement.setAlbum(StringUtils.trimToNull((String)track.get("Album")));
                        statement.setTime(track.get("Total Time") != null ? (Integer)track.get("Total Time") / 1000 : 0);
                        statement.setTrackNumber(track.get("Track Number") != null ? (Integer)track.get("Track Number") : 0);
                        statement.setFileName(file != null ? file.getAbsolutePath() : null);
                        myDataStoreSession.executeStatement(statement);
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
            boolean master = (Boolean)playlist.get("Master") != null && ((Boolean)playlist.get("Master")).booleanValue();
            boolean purchased = (Boolean)playlist.get("Purchased Music") != null && ((Boolean)playlist.get("Purchased Music")).booleanValue();
            boolean partyShuffle = (Boolean)playlist.get("Party Shuffle") != null && ((Boolean)playlist.get("Party Shuffle")).booleanValue();

            if (!master && !purchased && !partyShuffle) {
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