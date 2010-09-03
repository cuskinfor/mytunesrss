/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.httplivestreaming;

import de.codewave.utils.io.ExpiringCacheItem;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpLiveStreamingCacheItem extends ExpiringCacheItem {

    private Map<String, HttpLiveStreamingPlaylist> myPlaylists = new HashMap<String, HttpLiveStreamingPlaylist>();

    public HttpLiveStreamingCacheItem(String identifier, long timeout) {
        super(identifier, timeout);
    }

    public synchronized HttpLiveStreamingPlaylist getPlaylist(String identifier) {
        return myPlaylists.get(identifier);
    }

    public synchronized boolean putIfAbsent(String identifier, HttpLiveStreamingPlaylist playlist) {
        if (!myPlaylists.containsKey(identifier)) {
            myPlaylists.put(identifier, playlist);
            return true;
        }
        return false;
    }

    @Override
    protected synchronized void onItemExpired() {
        for (HttpLiveStreamingPlaylist playlist : myPlaylists.values()) {
            playlist.destroy();
        }
    }

    public synchronized void removePlaylist(String playlistIdentifier) {
        myPlaylists.remove(playlistIdentifier);
    }
}
