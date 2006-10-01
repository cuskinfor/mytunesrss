package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;

/**
 * de.codewave.mytunesrss.task.RecreateDatabaseTask
 */
public class RecreateDatabaseTask extends MyTunesRssTask {
    private DeleteDatabaseTask myDeleteDatabaseTask = new DeleteDatabaseTask(true);
    private InitializeDatabaseTask myInitializeDatabaseTask = new InitializeDatabaseTask();

    public void execute() throws Exception {
        MyTunesRss.STORE.destroy();
        myDeleteDatabaseTask.execute();
        myInitializeDatabaseTask.execute();
    }
}