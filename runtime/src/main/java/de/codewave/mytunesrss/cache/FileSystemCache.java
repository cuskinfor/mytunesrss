/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.cache;

import com.sun.mail.util.FolderClosedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileSystemCache implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemCache.class);

    private String myName;
    private File myBaseDir;
    private long myMaxSizeBytes;
    private long myIntervalMillis;
    private Thread myThread;

    public FileSystemCache(String name, File baseDir, long maxSizeBytes, long intervalMillis) throws IOException {
        myName = name;
        myBaseDir = baseDir;
        if (!myBaseDir.isDirectory()) {
            if (!myBaseDir.mkdirs()) {
                throw new IOException("Could not create cache dir \"" + myBaseDir + "\".");
            }
        }
        myMaxSizeBytes = maxSizeBytes;
        myIntervalMillis = intervalMillis;
    }

    public void init() {
        myThread = new Thread(this, "FileSystemCacheWorker-" + myName);
        myThread.start();
    }

    public void destroy() {
        myThread.interrupt();
        try {
            myThread.join();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for file system cache thread to die.", e);
        }
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        myMaxSizeBytes = maxSizeBytes;
    }

    public File getFileForName(String name) {
        return new File(myBaseDir, name);
    }

    public synchronized void deleteFilesByPrefix(String prefix) {
        for (File file : myBaseDir.listFiles()) {
            if (file.getName().startsWith(prefix)) {
                file.delete();
            }
        }
    }

    public synchronized void truncateCache() {
        LOGGER.debug("Truncating file system cache \"" + myName + "\".");
        List<File> files = Arrays.asList(myBaseDir.listFiles());
        Collections.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                }
                return 0;
            }
        });
        long size = 0;
        for (File file : files) {
            size += file.length();
        }
        LOGGER.debug("Found " + files.size() + " files with a total size of " + size + " bytes (" + myMaxSizeBytes + " bytes allowed).");
        if (size > myMaxSizeBytes) {
            for (File file : files) {
                long fileSize = file.length();
                if (file.delete()) {
                    LOGGER.debug("Cache file \"" + file.getName() + " deleted.");
                    size -= fileSize;
                    if (size < myMaxSizeBytes) {
                        break; // done deleting old files
                    }
                }
            }
        }
        LOGGER.debug("Done truncating file system cache \"" + myName + "\".");
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                truncateCache();
                Thread.sleep(myIntervalMillis);
            } catch (InterruptedException e) {
                // this is the expected way to end the thread
                return;
            }
        }
    }
}
