package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.io.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.filesystem.FileSystemLoaderr
 */
public class FileSystemLoader {
    private static final Log LOG = LogFactory.getLog(FileSystemLoader.class);

    public static String loadFromFileSystem(File baseDir, DataStoreSession storeSession, String previousBaseId, long lastUpdateTime)
            throws IOException, SQLException {
        Set<String> databaseIds = (Set<String>)storeSession.executeQuery(new FindTrackIdsQuery(TrackSource.FileSystem.name()));
        String baseDirId = null;
        MyTunesRssFileProcessor fileProcessor = null;
        if (baseDir != null) {
            baseDirId = IOUtils.getFileIdentifier(baseDir);
            if (baseDirId != null) {
                if (!baseDirId.equals(previousBaseId)) {
                    lastUpdateTime = Long.MIN_VALUE;// new base directory, update everything regardless of timestamps
                }
                fileProcessor = new MyTunesRssFileProcessor(baseDir, storeSession, lastUpdateTime);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Processing files from: \"" + baseDir + "\".");
                }
                IOUtils.processFiles(baseDir, fileProcessor, new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory() || FileSupportUtils.isSupported(file.getName());
                    }
                });
                FileProcessor playlistFileProcessor = new PlaylistFileProcessor(baseDir, storeSession);
                IOUtils.processFiles(baseDir, playlistFileProcessor, new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().toLowerCase().endsWith(".m3u");
                    }
                });
                if (LOG.isInfoEnabled()) {
                    LOG.info("Inserted/updated " + fileProcessor.getUpdatedCount() + " file system tracks.");
                }
                databaseIds.removeAll(fileProcessor.getExistingIds());
            }
        }
        if (!databaseIds.isEmpty()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Removing " + databaseIds.size() + " obsolete file system tracks.");
            }
            DeleteTrackStatement deleteTrackStatement = new DeleteTrackStatement(storeSession);
            for (String id : databaseIds) {
                deleteTrackStatement.setId(id);
                storeSession.executeStatement(deleteTrackStatement);
            }
        }
        if (fileProcessor != null && LOG.isDebugEnabled()) {
            LOG.info(fileProcessor.getExistingIds().size() + " file system tracks in the database.");
        }
        return baseDirId;
    }
}