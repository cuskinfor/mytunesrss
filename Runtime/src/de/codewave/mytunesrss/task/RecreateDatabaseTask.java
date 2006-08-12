/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.utils.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class RecreateDatabaseTask extends InitializeDatabaseTask {
    private static final Log LOG = LogFactory.getLog(RecreateDatabaseTask.class);

    public void execute() throws IOException {
        MyTunesRss.STORE.destroy();
        String pathname = null;
        pathname = ProgramUtils.getApplicationDataPath("MyTunesRSS");
        if (deleteRecursivly(new File(pathname + "/" + DataStore.DIRNAME))) {
            MyTunesRss.STORE.init();
            super.execute();
        } else {
            throw new IOException("Could not remove all old datebase files.");
        }
    }

    private boolean deleteRecursivly(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    if (!deleteRecursivly(subFile)) {
                        return false;
                    }
                }
            } else {
                return file.delete();
            }
        }
        return true;
    }

}