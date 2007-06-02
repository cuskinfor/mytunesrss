package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
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

    public static void loadFromFileSystem(File baseDir, DataStoreSession storeSession, long lastUpdateTime, Collection<String> databaseIds) throws IOException, SQLException {
        int trackCount = 0;
        MyTunesRssFileProcessor fileProcessor = null;
                if (baseDir != null && baseDir.exists() && baseDir.isDirectory()) {
                    fileProcessor = new MyTunesRssFileProcessor(baseDir, storeSession, lastUpdateTime);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Processing files from: \"" + baseDir + "\".");
                    }
                    IOUtils.processFiles(baseDir, fileProcessor, new FileFilter() {
                        public boolean accept(File file) {
                            return file.isDirectory() || FileSupportUtils.isSupported(file.getName());
                        }
                    });
                    FileProcessor playlistFileProcessor = new PlaylistFileProcessor(storeSession);
                    IOUtils.processFiles(baseDir, playlistFileProcessor, new FileFilter() {
                        public boolean accept(File file) {
                            return file.isDirectory() || file.getName().toLowerCase().endsWith(".m3u");
                        }
                    });
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Inserted/updated " + fileProcessor.getUpdatedCount() + " file system tracks.");
                    }
                    databaseIds.removeAll(fileProcessor.getExistingIds());
                    trackCount += fileProcessor.getExistingIds().size();
        }
        if (fileProcessor != null && LOG.isDebugEnabled()) {
            LOG.info(trackCount + " file system tracks in the database.");
        }
    }
}