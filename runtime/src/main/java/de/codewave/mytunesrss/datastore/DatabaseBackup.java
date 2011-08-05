/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore;

import de.codewave.mytunesrss.MyTunesRssUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseBackup implements Comparable<DatabaseBackup> {

    public static boolean isBackupFile(File file) {
        return !file.isFile() && file.getName().startsWith("h2-backup-") && file.getName().endsWith(".zip");
    }

    public static File createBackupFile() throws IOException {
        return new File(MyTunesRssUtils.getCacheDataPath(), "h2-backup-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip");
    }

    private long myDate;
    private File myFile;

    public DatabaseBackup(File file) throws IOException {
        if (!isBackupFile(file)) {
            throw new IllegalArgumentException("Specified file is not an H2 backup file.");
        }
        myFile = file;
        try {
            myDate = new SimpleDateFormat("'h2-backup-'yyyy-MM-dd_HH-mm-ss'.zip").parse(file.getName()).getTime();
        } catch (ParseException e) {
            throw new IOException("Could not parse data from database backup file name \"" + file.getName() + "\".");
        }
    }

    public long getDate() {
        return myDate;
    }

    public File getFile() {
        return myFile;
    }

    public int compareTo(DatabaseBackup databaseBackup) {
        return (int) Math.signum(myDate - databaseBackup.myDate);
    }

}
