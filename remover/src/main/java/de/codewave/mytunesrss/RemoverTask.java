/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.*;
import de.codewave.utils.swing.pleasewait.*;
import org.apache.commons.io.*;

import java.io.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.RemoverTask
 */
public class RemoverTask extends PleaseWaitTask {
    public void execute() throws Exception {
        deleteDirectory(new File(PrefsUtils.getCacheDataPath("MyTunesRSS")));
        deleteDirectory(new File(PrefsUtils.getCacheDataPath("MyTunesRSS3")));
        deleteDirectory(new File(PrefsUtils.getPreferencesDataPath("MyTunesRSS")));
        deleteDirectory(new File(PrefsUtils.getPreferencesDataPath("MyTunesRSS3")));
        Preferences.userRoot().node("/de/codewave/mytunesrss").removeNode();
        Preferences.userRoot().node("/de/codewave/mytunesrss3").removeNode();
    }

    private void deleteDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
        }
    }
}