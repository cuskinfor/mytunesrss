/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.httplivestreaming;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpLiveStreamingPlaylist {

    private List<File> myFiles = new ArrayList<File>();

    private AtomicBoolean myDone = new AtomicBoolean(false);

    private AtomicBoolean myFailed = new AtomicBoolean(false);

    private File baseDir;

    public HttpLiveStreamingPlaylist(File baseDir) {
        this.baseDir = baseDir;
        baseDir.mkdirs();
    }

    public File getBaseDir() {
        return baseDir;
    }

    public boolean isDone() {
        return myDone.get();
    }

    public void setDone(boolean done) {
        myDone.set(done);
    }

    public boolean isFailed() {
        return myFailed.get();
    }

    public void setFailed(boolean failed) {
        myFailed.set(failed);
    }

    public void addFile(File file) {
        if (myFailed.get() || myDone.get()) {
            FileUtils.deleteQuietly(file);
        } else {
            myFiles.add(file);
        }
    }

    public void destroy() {
        myFailed.set(true);
        for (File file : myFiles) {
            FileUtils.deleteQuietly(file);
        }
    }

    public int getSize() {
        return myFiles.size();
    }

    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n");
        sb.append("#EXT-X-TARGETDURATION:10\n");
        for (File file : myFiles) {
            sb.append("#EXTINF:10,\n");
            sb.append(getBaseDir().getName()).append("/").append(file.getName()).append("\n");
        }
        if (isDone() || isFailed()) {
            sb.append("#EXT-X-ENDLIST\n");
        }
        return sb.toString();
    }
}
