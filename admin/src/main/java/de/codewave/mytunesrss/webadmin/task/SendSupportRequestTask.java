/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.task;

import de.codewave.mytunesrss.DatasourceConfig;
import de.codewave.mytunesrss.DatasourceType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.task.SendSupportRequestRunnable;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.utils.io.ZipUtils;
import de.codewave.vaadin.component.ProgressWindow;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

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
