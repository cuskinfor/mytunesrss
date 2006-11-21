package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.utils.io.*;
import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.datastore.statement.*;

import java.io.*;
import java.util.*;
import java.sql.*;

import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

/**
 * de.codewave.mytunesrss.datastore.filesystem.PlaylistFileProcessor
 */
public class PlaylistFileProcessor implements FileProcessor {
    private static final Log LOG = LogFactory.getLog(PlaylistFileProcessor.class);

    private File myBaseDir;
    private DataStoreSession myDataStoreSession;

    public PlaylistFileProcessor(File baseDir, DataStoreSession storeSession) {
        myBaseDir = baseDir;
        myDataStoreSession = storeSession;
    }

    public void process(File playlistFile) {
        if (playlistFile.isFile()) {
            try {
                List<String> tracks = IOUtils.readTextFile(playlistFile, false);
                List<String> trackIds = new ArrayList<String>();
                for (String track : tracks) {
                    if (!track.trim().startsWith("#")) {
                        File trackFile = new File(playlistFile.getParentFile(), track.trim()); // relative track path
                        if (!trackFile.exists()) {
                            trackFile = new File(track.trim()); // absolute track path
                        }
                        String trackId = IOUtils.getFileIdentifier(trackFile);
                        if (StringUtils.isNotEmpty(trackId)) {
                            trackIds.add(trackId);
                        }
                    }
                }
                if (!trackIds.isEmpty()) {
                    SaveM3uFilePlaylistStatement statement = new SaveM3uFilePlaylistStatement();
                    statement.setId(IOUtils.getFileIdentifier(playlistFile));
                    statement.setName(IOUtils.getNameWithoutSuffix(playlistFile));
                    statement.setTrackIds(trackIds);
                    myDataStoreSession.executeStatement(statement);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Committing transaction after inserting playlist.");
                    }
                    myDataStoreSession.commitAndContinue();
                }
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not parse m3u playlist \"" + playlistFile + "\".", e);
                }
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not insert playlist from \"" + playlistFile + "\" into database.", e);
                }
            }
        }
    }
}