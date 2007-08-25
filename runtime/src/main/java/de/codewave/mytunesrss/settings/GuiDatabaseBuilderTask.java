package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.task.*;
import de.codewave.mytunesrss.*;

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
        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_STARTED);
        mySettings.updateConfigFromGui();
        try {
            super.execute();
            if (isExecuted()) {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED);
            } else {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED_NOT_RUN);
            }
        } finally {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.DATABASE_UPDATE_FINISHED);
        }
    }
}