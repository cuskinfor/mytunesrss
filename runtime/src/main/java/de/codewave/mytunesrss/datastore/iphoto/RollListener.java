/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.iphoto;

import de.codewave.mytunesrss.IphotoDatasourceConfig;
import de.codewave.mytunesrss.ShutdownRequestedException;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.PlaylistListenerr
 */
public class RollListener implements PListHandlerListener {
    private static final Logger LOG = LoggerFactory.getLogger(RollListener.class);

    private DataStoreSession myDataStoreSession;
    private Map<Long, String> myTrackIdToPersId;
    private Set<String> myExistingIds = new HashSet<String>();
    private LibraryListener myLibraryListener;
    private Thread myWatchdogThread;

    public RollListener(Thread watchdogThread, DataStoreSession dataStoreSession, LibraryListener libraryListener, Map<Long, String> trackIdToPersId, IphotoDatasourceConfig config) {
        myWatchdogThread = watchdogThread;
        myDataStoreSession = dataStoreSession;
        myTrackIdToPersId = trackIdToPersId;
        myLibraryListener = libraryListener;
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        throw new UnsupportedOperationException("method beforeDictPut of class ItunesLoader$PlaylistListener is not supported!");
    }

    public boolean beforeArrayAdd(List array, Object value) {
        insertPlaylist((Map) value);
        return false;
    }

    private void insertPlaylist(Map playlist) {
        if (myWatchdogThread.isInterrupted()) {
            throw new ShutdownRequestedException();
        }

        boolean folder = playlist.get("Folder") != null && ((Boolean) playlist.get("Folder")).booleanValue();

        String playlistId = playlist.get("Playlist Persistent ID") != null ? myLibraryListener.getLibraryId() + "_" + playlist.get(
                "Playlist Persistent ID").toString() :
                myLibraryListener.getLibraryId() + "_" + "PlaylistID" + playlist.get("Playlist ID").toString();
        String name = (String) playlist.get("Name");
        String containerId = playlist.get("Parent Persistent ID") != null ? myLibraryListener.getLibraryId() + "_" + playlist.get(
                "Parent Persistent ID") : null;
        List<Map> items = (List<Map>) playlist.get("Playlist Items");
        List<String> tracks = new ArrayList<String>();
        if (items != null && !items.isEmpty()) {
            for (Iterator<Map> itemIterator = items.iterator(); itemIterator.hasNext();) {
                Map item = itemIterator.next();
                Long trackId = (Long) item.get("Track ID");
                if (trackId != null && StringUtils.isNotEmpty(myTrackIdToPersId.get(trackId))) {
                    tracks.add(myTrackIdToPersId.get(trackId));
                }
            }
        }
        if (!tracks.isEmpty()) {
            SavePlaylistStatement statement = new SaveITunesPlaylistStatement(folder);
            statement.setId(playlistId);
            statement.setName(name);
            statement.setTrackIds(tracks);
            statement.setContainerId(containerId);
            try {
                if (myDataStoreSession.executeQuery(new FindPlaylistQuery(Arrays.asList(PlaylistType.ITunes, PlaylistType.ITunesFolder),
                        playlistId,
                        null,
                        true)).getResultSize() > 0) {
                    statement.setUpdate(true);
                }
                myDataStoreSession.executeStatement(statement);
                myExistingIds.add(playlistId);
                DatabaseBuilderCallable.doCheckpoint(myDataStoreSession, true);
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not insert/update playlist \"" + name + "\" into database.", e);
                }
            }
        }
    }

    public Collection<String> getExistingIds() {
        return myExistingIds;
    }
}
