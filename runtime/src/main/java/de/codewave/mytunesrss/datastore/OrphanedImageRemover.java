package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class OrphanedImageRemover {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OrphanedImageRemover.class);
    
    private MVStore myStore;
    
    public void init() throws SQLException {
        StopWatch.start("Initializing orphaned image remover");
        try {
            myStore = MyTunesRssUtils.getMvStoreBuilder("orphaned-image-remover").compress().open();
            final Map<String, Byte> hashes = myStore.openMap("hashes");
            DataStoreQuery<Void> query = new DataStoreQuery<Void>() {
                @Override
                public Void execute(Connection connection) throws SQLException {
                    ResultSet rs = MyTunesRssUtils.createStatement(connection, "getAllImageHashes").executeQuery();
                    while (rs.next()) {
                        String hash = rs.getString(1);
                        if (StringUtils.isNotBlank(hash)) {
                            hashes.put(hash, (byte) 0);
                        }
                    }
                    return null;
                }
            };
            query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
            MyTunesRss.STORE.executeQuery(query);
        } finally {
            StopWatch.stop();
        }
    }
    
    public void remove() throws SQLException {
        StopWatch.start("Removing orphaned images");
        try {
            final Map<String, Byte> hashes = myStore.openMap("hashes");
            DataStoreQuery<Void> query = new DataStoreQuery<Void>() {
                @Override
                public Void execute(Connection connection) throws SQLException {
                    ResultSet rs = MyTunesRssUtils.createStatement(connection, "getAllImageHashes").executeQuery();
                    while (rs.next()) {
                        String hash = rs.getString(1);
                        if (StringUtils.isNotBlank(hash)) {
                            hashes.remove(hash);
                        }
                    }
                    return null;
                }
            };
            query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
            MyTunesRss.STORE.executeQuery(query);
            LOGGER.info("Removing " + hashes.size() + " orphaned images.");
            File cacheDataPath = new File(MyTunesRss.CACHE_DATA_PATH);
            for (String hash : hashes.keySet()) {
                if (StringUtils.isNotBlank(hash)) {
                    LOGGER.debug("Deleting image \"" + hash + "\".");
                    File imageDir = MyTunesRssUtils.getImageDir(hash);
                    if (imageDir.isDirectory()) {
                        try {
                            LOGGER.debug("Deleting image directory \"" + imageDir.getAbsolutePath() + "\".");
                            FileUtils.deleteDirectory(imageDir);
                            for (File dir = imageDir.getParentFile(); !dir.equals(cacheDataPath) && dir.listFiles().length == 0; dir = dir.getParentFile()) {
                                LOGGER.debug("Deleting image directory \"" + dir.getAbsolutePath() + "\".");
                                FileUtils.deleteDirectory(dir);
                            }
                        } catch (IOException ignored) {
                            LOGGER.info("Could not remove orphaned images from \"" + imageDir.getAbsolutePath() + "\".");
                        }
                    }
                }
            }
            hashes.clear();
        } finally {
            StopWatch.stop();
        }
    }
    
    public void destroy() {
        if (myStore != null) {
            try {
                myStore.close();
            } finally {
                MyTunesRssUtils.removeMvStoreFile("orphaned-image-remover");
            }
        }
    }
}
