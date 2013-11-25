package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.io.FileUtils;
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
        myStore = MyTunesRssUtils.getMvStoreBuilder("orphaned-image-remover").compressData().open();
        try {
            DataStoreQuery<Void> query = new DataStoreQuery<Void>() {
                @Override
                public Void execute(Connection connection) throws SQLException {
                    Map<String, Byte> hashes = myStore.openMap("hashes");
                    ResultSet rs = MyTunesRssUtils.createStatement(connection, "getAllImageHashes").executeQuery();
                    while (rs.next()) {
                        hashes.put(rs.getString("hash"), Byte.valueOf((byte) 0));
                    }
                    return null;
                }
            };
            query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
            MyTunesRss.STORE.executeQuery(query);
        } catch (SQLException e) {
            destroy();
            throw e;
        } finally {
            myStore.commit();
            myStore.close();
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
                    ResultSet rs = MyTunesRssUtils.createStatement(connection, "").executeQuery();
                    while (rs.next()) {
                        hashes.remove(rs.getString("hash"));
                    }
                    return null;
                }
            };
            query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
            MyTunesRss.STORE.executeQuery(query);
            for (String hash : hashes.keySet()) {
                File imageDir = MyTunesRssUtils.getImageDir(hash);
                try {
                    FileUtils.deleteDirectory(imageDir);
                } catch (IOException e) {
                    LOGGER.info("Could not remove orphaned images from \"" + imageDir.getAbsolutePath() + "\".");
                }
            }
        } finally {
            myStore.close();
            StopWatch.stop();
        }
    }
    
    public void destroy() {
        MyTunesRssUtils.removeMvStoreFile("orphaned-image-remover");
    }
}
