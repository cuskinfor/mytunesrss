package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.WatchfolderDatasourceConfig;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.SaveM3uFilePlaylistStatement;
import de.codewave.mytunesrss.datastore.updatequeue.DataStoreStatementEvent;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.io.FileProcessor;
import de.codewave.utils.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.filesystem.PlaylistFileProcessor
 */
public class PlaylistFileProcessor implements FileProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PlaylistFileProcessor.class);

    private DatabaseUpdateQueue myQueue;
    private Set<String> myExistingTrackIds;
    private WatchfolderDatasourceConfig myDatasourceConfig;

    public PlaylistFileProcessor(WatchfolderDatasourceConfig datasourceConfig, DatabaseUpdateQueue queue, Set<String> existingTrackIds) {
        myDatasourceConfig = datasourceConfig;
        myQueue = queue;
        myExistingTrackIds = existingTrackIds;
    }

    public void process(File playlistFile) {
        if (playlistFile.isFile()) {
            try {
                String id = "file_" + IOUtils.getFilenameHash(playlistFile);
                String[] tracks = FileUtils.readFileToString(playlistFile).split("[\\r\\n]");
                List<String> trackIds = new ArrayList<>();
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
                    SaveM3uFilePlaylistStatement statement = new SaveM3uFilePlaylistStatement(myDatasourceConfig.getId());
                    statement.setId(id);
                    statement.setName(FilenameUtils.getBaseName(playlistFile.getName()));
                    statement.setTrackIds(trackIds);
                    if (MyTunesRss.STORE.getQueryResultSize(new FindPlaylistQuery(Collections.singletonList(PlaylistType.M3uFile), id, null, true)) > 0) {
                        statement.setUpdate(true);
                    }
                    myQueue.offer(new DataStoreStatementEvent(statement, true, "Could not insert/update playlist from \"" + playlistFile + "\" into database."));
                }
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not parse m3u playlist \"" + playlistFile + "\".", e);
                }
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not insert/update playlist from \"" + playlistFile + "\" into database.", e);
                }
            } catch (RuntimeException e) {
                LOG.error("Could not insert/update playlist from \"" + playlistFile + "\".", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
