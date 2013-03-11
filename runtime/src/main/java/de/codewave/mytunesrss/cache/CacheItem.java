package de.codewave.mytunesrss.cache;

class CacheItem {
    private long myLastAccessTime;
    private long mySize;
    private String myId;

    CacheItem(String id, long size, long lastAccessTime) {
        myId = id;
        mySize = size;
        myLastAccessTime = lastAccessTime;
    }

    public long getLastAccessTime() {
        return myLastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        myLastAccessTime = lastAccessTime;
    }

    public long getSize() {
        return mySize;
    }

    public void setSize(long size) {
        mySize = size;
    }

    public String getId() {
        return myId;
    }

    public void setId(String id) {
        myId = id;
    }
}
