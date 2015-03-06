package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * de.codewave.mytunesrss.task.InitializeDatabaseCallable
 */
public class DeleteDatabaseFilesCallable implements Callable<Void> {
    @Override
    public Void call() throws IOException {
        String filename = "h2";
        String pathname = MyTunesRss.CACHE_DATA_PATH;
        FileUtils.deleteQuietly(new File(pathname + "/" + filename));
        return null;
    }
}