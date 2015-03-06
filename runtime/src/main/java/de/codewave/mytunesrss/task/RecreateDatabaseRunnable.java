package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.DropAllTablesStatement;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

/**
 * de.codewave.mytunesrss.task.RecreateDatabaseRunnable
 */
public class RecreateDatabaseRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecreateDatabaseRunnable.class);

    private InitializeDatabaseCallable myInitializeDatabaseCallable = new InitializeDatabaseCallable();

    @Override
    public void run() {
        MyTunesRss.EXECUTOR_SERVICE.cancelImageGenerators();
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Recreating the database.");
            }
            dropAllTables();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Destroying store for recreation.");
            }
            MyTunesRss.STORE.destroy();
            myInitializeDatabaseCallable.call();
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        } catch (SQLException | IOException e) {
            LOGGER.error("Could not recreate database.", e);
        } finally {
            MyTunesRss.EXECUTOR_SERVICE.scheduleImageGenerators();
        }
    }

    private void dropAllTables() throws SQLException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Dropping all tables.");
        }
        DataStoreSession storeSession = MyTunesRss.STORE.getTransaction();
        try {
            storeSession.executeStatement(new DropAllTablesStatement());
            storeSession.commit();
        } catch (SQLException e) {
            LOGGER.error("Could not drop all tables.", e);
            if (MyTunesRss.CONFIG.isDefaultDatabase()) {
                MyTunesRss.CONFIG.setDeleteDatabaseOnExit(true);
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.shutdownAndDeleteDatabase"));
                MyTunesRssUtils.shutdownGracefully();
            } else {
                throw e;
            }
        } finally {
            storeSession.rollback();
        }
    }

}