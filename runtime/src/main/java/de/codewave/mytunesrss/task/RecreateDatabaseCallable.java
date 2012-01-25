package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.RecreateDatabaseCallable
 */
public class RecreateDatabaseCallable implements Callable<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecreateDatabaseCallable.class);

    private DropAllTablesCallable myDropAllTablesCallable = new DropAllTablesCallable();
    private InitializeDatabaseCallable myInitializeDatabaseCallable = new InitializeDatabaseCallable();

    public Void call() throws Exception {
        LOGGER.debug("Recreating the database.");
        myDropAllTablesCallable.call();
        LOGGER.debug("Destroying store for recreation.");
        MyTunesRss.STORE.destroy();
        myInitializeDatabaseCallable.call();
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED));
        return null;
    }
}