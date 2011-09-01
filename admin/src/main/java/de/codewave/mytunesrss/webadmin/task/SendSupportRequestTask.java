/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.task;

import de.codewave.mytunesrss.task.SendSupportRequestRunnable;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.vaadin.component.ProgressWindow;

public class SendSupportRequestTask extends SendSupportRequestRunnable implements ProgressWindow.Task {
    private MainWindow myMainWindow;

    public SendSupportRequestTask(MainWindow mainWindow, String name, String email, String comment, boolean includeItunesXml) {
        super(name, email, comment, includeItunesXml);
        myMainWindow = mainWindow;
    }

    public int getProgress() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void onWindowClosed() {
        if (isSuccess()) {
            myMainWindow.showInfo("supportConfigPanel.info.supportRequestSent");
        } else {
            myMainWindow.showError("supportConfigPanel.error.supportRequestFailed");
        }
    }
}
