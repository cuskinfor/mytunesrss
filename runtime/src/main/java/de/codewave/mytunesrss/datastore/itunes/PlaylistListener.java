package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.mytunesrss.config.ItunesDatasourceConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.ShutdownRequestedException;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.SavePlaylistStatement;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreStatementEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.xml.PListHandlerListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.PlaylistListenerr
 */
public class PlaylistListener implements PListHandlerListener {
    private static final Logger LOG = LoggerFactory.getLogger(PlaylistListener.class);

    private DatabaseUpdateQueue myQueue;
    private Map<Long, String> myTrackIdToPersId;
    private LibraryListener myLibraryListener;
    private Thread myWatchdogThread;
    private Set<ItunesPlaylistType> myIgnores;
    ItunesDatasourceConfig myDatasourceConfig;

    public PlaylistListener(ItunesDatasourceConfig datasourceConfig, Thread watchdogThread, DatabaseUpdateQueue queue, LibraryListener libraryListener, Map<Long, String> trackIdToPersId, ItunesDatasourceConfig config) {
        myDatasourceConfig = datasourceConfig;
        myWatchdogThread = watchdogThread;
        myQueue = queue;
        myTrackIdToPersId = trackIdToPersId;
        myLibraryListener = libraryListener;
        myIgnores = config.getIgnorePlaylists();
        myIgnores.add(ItunesPlaylistType.Master); // always ignore "Master" playlist
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        throw new UnsupportedOperationException("method beforeDictPut of class ItunesLoader$PlaylistListener is not supported!");
    }

    public boolean beforeArrayAdd(List array, Object value) {
        try {
            insertPlaylist((Map) value);
        } catch (RuntimeException e) {
            LOG.error("Could not process playlist.", e);
        }
        return false;
    }

    private void insertPlaylist(Map playlist) {
        if (myWatchdogThread.isInterrupted()) {
            Thread.currentThread().interrupt();
            throw new ShutdownRequestedException();
        }

        boolean ignore = false;
        for (ItunesPlaylistType type : myIgnores) {
            ignore = playlist.get(type.toString()) != null && ((Boolean) playlist.get(type.toString())).booleanValue();
            if (ignore) {
                break;
            }
        }
        boolean folder = playlist.get("Folder") != null && ((Boolean) playlist.get("Folder")).booleanValue();
        boolean smart = playlist.get("Smart Info") != null;

        if (!ignore && (!smart || !myIgnores.contains(ItunesPlaylistType.SmartPlaylists))) {
            String playlistId = playlist.get("Playlist Persistent ID") != null ? myLibraryListener.getLibraryId() + "_" + playlist.get(
                    "Playlist Persistent ID").toString() :
                    myLibraryListener.getLibraryId() + "_" + "PlaylistID" + playlist.get("Playlist ID").toString();
            String name = (String) playlist.get("Name");
            String containerId = playlist.get("Parent Persistent ID") != null ? myLibraryListener.getLibraryId() + "_" + playlist.get(
                    "Parent Persistent ID") : null;
            List<Map> items = (List<Map>) playlist.get("Playlist Items");
            List<String> tracks = new ArrayList<>();
            if (items != null && !items.isEmpty()) {
                for (Iterator<Map> itemIterator = items.iterator(); itemIterator.hasNext();) {
                    Map item = itemIterator.next();
                    Long trackId = (Long) item.get("Track ID");
                    if (trackId != null && StringUtils.isNotBlank(myTrackIdToPersId.get(trackId))) {
                        tracks.add(myTrackIdToPersId.get(trackId));
                    }
                }
            }
            if (!tracks.isEmpty()) {
                SavePlaylistStatement statement = new SaveITunesPlaylistStatement(myDatasourceConfig.getId(), folder);
                statement.setId(playlistId);
                statement.setName(name);
                statement.setTrackIds(tracks);
                statement.setContainerId(containerId);
                try {
                    if (MyTunesRss.STORE.getQueryResultSize(new FindPlaylistQuery(Arrays.asList(PlaylistType.ITunes, PlaylistType.ITunesFolder),
                            playlistId,
                            null,
                            true)) > 0) {
                        statement.setUpdate(true);
                    }
                    myQueue.offer(new DataStoreStatementEvent(statement, true));
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not insert/update playlist \"" + name + "\" into database.", e);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
