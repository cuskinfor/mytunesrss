package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.SaveM3uFilePlaylistStatement;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.io.FileProcessor;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.filesystem.PlaylistFileProcessor
 */
public class PlaylistFileProcessor implements FileProcessor {
    private static final Log LOG = LogFactory.getLog(PlaylistFileProcessor.class);

    private DataStoreSession myDataStoreSession;
    private Collection<String> myExistingIds = new HashSet<String>();
    private Set<String> myExistingTrackIds;

    public PlaylistFileProcessor(DataStoreSession storeSession, Set<String> existingTrackIds) {
        myDataStoreSession = storeSession;
        myExistingTrackIds = existingTrackIds;
    }

    public void process(File playlistFile) {
        if (playlistFile.isFile()) {
            try {
                String id = "file_" + IOUtils.getFilenameHash(playlistFile);
                String[] tracks = FileUtils.readFileToString(playlistFile).split("[\\r\\n]");
                List<String> trackIds = new ArrayList<String>();
                for (String track : tracks) {
                    if (!track.trim().startsWith("#")) {
                        File trackFile = new File(playlistFile.getParentFile(), track.trim());// relative track path
                        if (!trackFile.exists()) {
                            trackFile = new File(track.trim());// absolute track path
                        }
                        String trackId = "file_" + IOUtils.getFilenameHash(trackFile);
                        if (StringUtils.isNotEmpty(trackId) && myExistingTrackIds.contains(trackId)) {
                            trackIds.add(trackId);
                        }
                    }
                }
                if (!trackIds.isEmpty()) {
                    SaveM3uFilePlaylistStatement statement = new SaveM3uFilePlaylistStatement();
                    statement.setId(id);
                    statement.setName(FilenameUtils.getBaseName(playlistFile.getName()));
                    statement.setTrackIds(trackIds);
                    if (myDataStoreSession.executeQuery(new FindPlaylistQuery(PlaylistType.M3uFile, id, true)).getResultSize() > 0) {
                        statement.setUpdate(true);
                    }
                    myDataStoreSession.executeStatement(statement);
                    myExistingIds.add(id);
                    DatabaseBuilderTask.doCheckpoint(myDataStoreSession, true);
                    MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_PLAYLIST_UPDATED);
                }
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not parse m3u playlist \"" + playlistFile + "\".", e);
                }
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not insert/update playlist from \"" + playlistFile + "\" into database.", e);
                }
            }
        }
    }

    public Collection<String> getExistingIds() {
        return myExistingIds;
    }
}