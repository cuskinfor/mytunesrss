package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.task.*;

/**
 * de.codewave.mytunesrss.settings.GuiDatabaseBuilderTask
 */
public class GuiDatabaseBuilderTask extends DatabaseBuilderTask {
    Database myDatabase;

    public GuiDatabaseBuilderTask(Database database) {
        super();
        myDatabase = database;
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        if (isExecuted()) {
            myDatabase.refreshLastUpdate();
        }
    }
}