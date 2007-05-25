package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.*;

import java.io.*;
import java.sql.*;
import java.util.prefs.*;

import org.apache.commons.io.*;

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