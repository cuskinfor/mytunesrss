package de.codewave.mytunesrss;

import com.google.common.io.Files;
import de.codewave.mytunesrss.config.DatabaseType;
import de.codewave.mytunesrss.config.MyTunesRssConfig;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement;
import de.codewave.utils.sql.DataStoreSession;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.UUID;

/**
 * de.codewave.mytunesrss.datastore.MyTunesRssDatastoreTestUtils
 */
public class MyTunesRssTestUtils {

    public static File createTempDir() throws IOException {
        File tempDir = File.createTempFile("mytunesrss-test-", ".tmp");
        tempDir.delete();
        tempDir.mkdirs();
        return tempDir;
    }

    public static void before() throws ClassNotFoundException, IOException, SQLException, NoSuchAlgorithmException {
        MyTunesRss.VERSION = "1.0.0";
        MyTunesRss.CACHE_DATA_PATH = createTempDir().getAbsolutePath();
        MyTunesRss.PREFERENCES_DATA_PATH = createTempDir().getAbsolutePath();
        MyTunesRss.LUCENE_TRACK_SERVICE.deleteLuceneIndex();
        MyTunesRss.CONFIG = new MyTunesRssConfig();
        MyTunesRss.CONFIG.setDatabaseType(DatabaseType.h2);
        MyTunesRss.CONFIG.setDatabaseConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        MyTunesRss.CONFIG.setDatabaseUser("sa");
        MyTunesRss.CONFIG.setDatabasePassword("");
        Class.forName("org.h2.Driver");
        MyTunesRss.STORE = new MyTunesRssDataStore();
        MyTunesRss.STORE.init();
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            session.executeStatement(new CreateAllTablesStatement());
            session.commit();
        } finally {
            session.rollback();
        }
    }

    public static void after() {
        MyTunesRss.STORE.destroy();
    }

}
