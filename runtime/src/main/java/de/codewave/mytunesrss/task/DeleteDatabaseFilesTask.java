package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssTask;
import de.codewave.utils.PrefsUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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