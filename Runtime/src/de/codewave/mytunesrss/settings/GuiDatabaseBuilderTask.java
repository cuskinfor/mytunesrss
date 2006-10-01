package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.task.*;

/**
 * de.codewave.mytunesrss.settings.GuiDatabaseBuilderTask
 */
public class GuiDatabaseBuilderTask extends DatabaseBuilderTask {
    Options myOptions;

    public GuiDatabaseBuilderTask(Options options) {
        super();
        myOptions = options;
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        myOptions.refreshLastUpdate();
    }
}