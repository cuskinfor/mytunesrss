package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.task.*;

/**
 * de.codewave.mytunesrss.settings.GuiDatabaseBuilderTask
 */
public class GuiDatabaseBuilderTask extends DatabaseBuilderTask {
    General myGeneral;

    public GuiDatabaseBuilderTask(General general) {
        super();
        myGeneral = general;
    }

    @Override
    public void execute() throws Exception {
        super.execute();
        myGeneral.refreshLastUpdate();
    }
}