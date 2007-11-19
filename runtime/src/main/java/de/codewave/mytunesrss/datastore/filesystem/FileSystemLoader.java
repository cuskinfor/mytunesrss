package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.mytunesrss.*;
import de.codewave.utils.io.*;
import de.codewave.utils.io.IOUtils;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;
import org.apache.commons.io.*;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.filesystem.FileSystemLoaderr
 */
public class FileSystemLoader {
    private static final Log LOG = LogFactory.getLog(FileSystemLoader.class);

    public static void loadFromFileSystem(File baseDir, DataStoreSession storeSession, long lastUpdateTime, Collection<String> trackIds, Collection<String> playlistIds) throws IOException, SQLException {
        MyTunesRssFileProcessor fileProcessor = null;
                if (baseDir != null && baseDir.isDirectory()) {
                    fileProcessor = new MyTunesRssFileProcessor(baseDir, storeSession, lastUpdateTime, trackIds);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Processing files from: \"" + baseDir + "\".");
                    }
                    IOUtils.processFiles(baseDir, fileProcessor, new FileFilter() {
                        public boolean accept(File file) {
                            return file.isDirectory() || FileSupportUtils.isSupported(file.getName());
                        }
                    });
                    PlaylistFileProcessor playlistFileProcessor = new PlaylistFileProcessor(storeSession, fileProcessor.getExistingIds());
                    IOUtils.processFiles(baseDir, playlistFileProcessor, new FileFilter() {
                        public boolean accept(File file) {
                            return file.isDirectory() || "m3u".equals(FilenameUtils.getExtension(file.getName().toLowerCase()));
                        }
                    });
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Inserted/updated " + fileProcessor.getUpdatedCount() + " file system tracks.");
                    }
                    playlistIds.removeAll(playlistFileProcessor.getExistingIds());
        }
    }
}