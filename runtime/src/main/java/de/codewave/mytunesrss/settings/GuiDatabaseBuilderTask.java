package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.task.DatabaseBuilderTask;

/**
 * de.codewave.mytunesrss.settings.GuiDatabaseBuilderTask
 */
public class GuiDatabaseBuilderTask extends DatabaseBuilderTask {
    Settings mySettings;

    public GuiDatabaseBuilderTask(Settings settings) {
        super();
        mySettings = settings;
    }

    @Override
    public void execute() throws Exception {
        mySettings.updateConfigFromGui();
        super.execute();
    }
}