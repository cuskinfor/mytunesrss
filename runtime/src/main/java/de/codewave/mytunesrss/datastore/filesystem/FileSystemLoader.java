package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.mytunesrss.ShutdownRequestedException;
import de.codewave.mytunesrss.config.WatchfolderDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import de.codewave.utils.io.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.filesystem.FileSystemLoaderr
 */
public class FileSystemLoader {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemLoader.class);

    public static void loadFromFileSystem(final Thread watchdogThread, final WatchfolderDatasourceConfig datasource, DatabaseUpdateQueue queue, Map<String, Long> trackTsUpdate, Map<String, String> trackSourceId, Map<String, Long> photoTsUpdate, Map<String, String> photoSourceId, MVStore mvStore) throws SQLException {
        MyTunesRssFileProcessor fileProcessor = null;
        File baseDir = new File(datasource.getDefinition());
        if (baseDir != null && baseDir.isDirectory()) {
            fileProcessor = new MyTunesRssFileProcessor(datasource, queue, trackTsUpdate, trackSourceId, photoTsUpdate, photoSourceId, mvStore);
            if (LOG.isInfoEnabled()) {
                LOG.info("Processing files from: \"" + baseDir + "\".");
            }
            IOUtils.processFiles(baseDir, fileProcessor, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (watchdogThread.isInterrupted()) {
                        Thread.currentThread().interrupt();
                        throw new ShutdownRequestedException();
                    }
                    return file.isDirectory() || (datasource.isIncluded(file) && !isPlaylist(file));
                }
            });
            for (String id : fileProcessor.getExistingIds()) {
                trackTsUpdate.remove(id);
            }
            for (String id : fileProcessor.getExistingIds()) {
                photoTsUpdate.remove(id);
            }
            if (datasource.isImportPlaylists()) {
                PlaylistFileProcessor playlistFileProcessor = new PlaylistFileProcessor(datasource, queue, fileProcessor.getExistingIds());
                IOUtils.processFiles(baseDir, playlistFileProcessor, new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (watchdogThread.isInterrupted()) {
                            Thread.currentThread().interrupt();
                            throw new ShutdownRequestedException();
                        }
                        return file.isDirectory() || (datasource.isIncluded(file) && isPlaylist(file));
                    }
                });
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Inserted/updated " + fileProcessor.getUpdatedCount() + " file system tracks.");
            }
        }
    }

    private static boolean isPlaylist(File file) {
        return "m3u".equals(FilenameUtils.getExtension(file.getName().toLowerCase()));
    }
}
