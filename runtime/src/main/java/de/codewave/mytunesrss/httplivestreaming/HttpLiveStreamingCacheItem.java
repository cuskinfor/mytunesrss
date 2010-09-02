/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.httplivestreaming;

import de.codewave.utils.io.ExpiringCacheItem;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpLiveStreamingCacheItem extends ExpiringCacheItem {

    private List<File> myFiles = new ArrayList<File>();

    private AtomicBoolean myDone = new AtomicBoolean(false);

    private AtomicBoolean myFailed = new AtomicBoolean(false);

    public HttpLiveStreamingCacheItem(String identifier, long timeout) {
        super(identifier, timeout);
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
        myFiles.add(file);
    }

    public int getPlaylistSize() {
        return myFiles.size();
    }

    public String getPlaylist() {
        StringBuilder sb = new StringBuilder();
        // TODO
        return sb.toString();
    }

    @Override
    protected void onItemExpired() {
        try {
            while (!myDone.get() && !myFailed.get()) {
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            // we have been interrupted, so we do not wait anymore but cleanup what we have so far
        }
        for (File file : myFiles) {
            FileUtils.deleteQuietly(file);
        }
    }
}
