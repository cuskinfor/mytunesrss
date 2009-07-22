package de.codewave.mytunesrss.task;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import de.codewave.mytunesrss.MyTunesRssTask;
import de.codewave.mytunesrss.MyTunesRssUtils;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseTask
 */
public class DeleteDatabaseFilesTask extends MyTunesRssTask {
    public void execute() throws IOException {
        String filename = "h2/MyTunesRSS";
        String pathname = MyTunesRssUtils.getCacheDataPath();
        FileUtils.deleteDirectory(new File(pathname + "/" + filename));
    }
}