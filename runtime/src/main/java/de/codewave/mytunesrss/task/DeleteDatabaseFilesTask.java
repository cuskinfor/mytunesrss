package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.utils.*;
import org.apache.commons.io.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class DeleteDatabaseFilesTask extends MyTunesRssTask {
    public void execute() throws IOException {
        String filename = "h2/MyTunesRSS";
        String pathname = PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        FileUtils.deleteDirectory(new File(pathname + "/" + filename));
    }
}