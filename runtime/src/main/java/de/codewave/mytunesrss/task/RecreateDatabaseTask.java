package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssTask;
import de.codewave.mytunesrss.MyTunesRss;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.task.RecreateDatabaseTask
 */
public class RecreateDatabaseTask extends MyTunesRssTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecreateDatabaseTask.class);

    private DropAllTablesTask myDropAllTablesTask = new DropAllTablesTask();
    private InitializeDatabaseTask myInitializeDatabaseTask = new InitializeDatabaseTask();

    public void execute() throws Exception {
        LOGGER.debug("Recreating the database.");
        myDropAllTablesTask.execute();
        LOGGER.debug("Destroying store for recreation.");
        MyTunesRss.STORE.destroy();
        myInitializeDatabaseTask.execute();
        MyTunesRss.LUCENE_TRACK_SERVICE.indexAllTracks();
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED);
    }
}