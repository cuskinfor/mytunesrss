package de.codewave.mytunesrss.datastore.itunes;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.xml.*;
import org.apache.commons.logging.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.itunes.PlaylistListenerr
 */
public class PlaylistListener implements PListHandlerListener {
    private static final Log LOG = LogFactory.getLog(PlaylistListener.class);

    private DataStoreSession myDataStoreSession;
    private Map<Long, String> myTrackIdToPersId;
    private Set<String> myExistingIds = new HashSet<String>();
    private LibraryListener myLibraryListener;

    public PlaylistListener(DataStoreSession dataStoreSession, LibraryListener libraryListener, Map<Long, String> trackIdToPersId) {
        myDataStoreSession = dataStoreSession;
        myTrackIdToPersId = trackIdToPersId;
        myLibraryListener = libraryListener;
    }

    public boolean beforeDictPut(Map dict, String key, Object value) {
        throw new UnsupportedOperationException("method beforeDictPut of class ItunesLoader$PlaylistListener is not supported!");
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
            String playlistId =
                    playlist.get("Playlist Persistent ID") != null ? myLibraryListener.getLibraryId() + "_" + playlist.get("Playlist Persistent ID").toString() :
                            myLibraryListener.getLibraryId() + "_" + "PlaylistID" + playlist.get(
                            "Playlist ID").toString();
            String name = (String)playlist.get("Name");
            List<Map> items = (List<Map>)playlist.get("Playlist Items");
            List<String> tracks = new ArrayList<String>();
            if (items != null && !items.isEmpty()) {
                for (Iterator<Map> itemIterator = items.iterator(); itemIterator.hasNext();) {
                    Map item = itemIterator.next();
                    tracks.add(myTrackIdToPersId.get(item.get("Track ID")));
                }
            }
            if (!tracks.isEmpty()) {
                DeletePlaylistStatement deleteStatement = new DeletePlaylistStatement();
                deleteStatement.setId(playlistId);
                SavePlaylistStatement statement = new SaveITunesPlaylistStatement();
                statement.setId(playlistId);
                statement.setName(name);
                statement.setTrackIds(tracks);
                try {
                    if (!myDataStoreSession.executeQuery(new FindPlaylistQuery(PlaylistType.ITunes, playlistId)).isEmpty()) {
                        statement.setUpdate(true);
                    }
                    myDataStoreSession.executeStatement(statement);
                    myExistingIds.add(playlistId);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Committing transaction after inserting playlist.");
                    }
                    myDataStoreSession.commit();
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not insert playlist \"" + name + "\" into database.", e);
                    }
                }
            }
        }
    }

    public Collection<String> getExistingIds() {
        return myExistingIds;
    }
}
