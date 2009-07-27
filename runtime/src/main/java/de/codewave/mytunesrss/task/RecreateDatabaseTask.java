package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.MyTunesRssTask;

/**
 * de.codewave.mytunesrss.task.RecreateDatabaseTask
 */
public class RecreateDatabaseTask extends MyTunesRssTask {
    private DropAllTablesTask myDropAllTablesTask = new DropAllTablesTask();
    private InitializeDatabaseTask myInitializeDatabaseTask = new InitializeDatabaseTask(true);

    public void execute() throws Exception {
        myDropAllTablesTask.execute();
        myInitializeDatabaseTask.execute();
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED);
    }
}