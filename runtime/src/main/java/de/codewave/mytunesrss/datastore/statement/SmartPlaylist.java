package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SmartPlaylist
 */
public class SmartPlaylist {
    private Playlist myPlaylist;
    private SmartInfo mySmartInfo;

    public Playlist getPlaylist() {
        return myPlaylist;
    }

    public void setPlaylist(Playlist playlist) {
        myPlaylist = playlist;
    }

    public SmartInfo getSmartInfo() {
        return mySmartInfo;
    }

    public void setSmartInfo(SmartInfo smartInfo) {
        mySmartInfo = smartInfo;
    }
}