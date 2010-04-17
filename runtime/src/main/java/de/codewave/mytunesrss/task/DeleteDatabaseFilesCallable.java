package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseCallable
 */
public class DeleteDatabaseFilesCallable implements Callable<Void> {
    public Void call() throws IOException {
        String filename = "h2";
        String pathname = MyTunesRssUtils.getCacheDataPath();
        FileUtils.deleteDirectory(new File(pathname + "/" + filename));
        return null;
    }
}