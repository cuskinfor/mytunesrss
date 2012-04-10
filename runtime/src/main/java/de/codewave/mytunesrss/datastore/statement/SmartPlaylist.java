package de.codewave.mytunesrss.datastore.statement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.SmartPlaylist
 */
public class SmartPlaylist {
    private Playlist myPlaylist;
    private Collection<SmartInfo> mySmartInfos;

    public Playlist getPlaylist() {
        return myPlaylist;
    }

    public void setPlaylist(Playlist playlist) {
        myPlaylist = playlist;
    }

    public Collection<SmartInfo> getSmartInfos() {
        return mySmartInfos;
    }

    public void setSmartInfos(Collection<SmartInfo> smartInfos) {
        mySmartInfos = smartInfos;
    }

    public Map<String, String> getSmartFields() {
        Map<String, String> fields = new HashMap<String, String>();
        for (SmartInfo smartInfo : mySmartInfos) {
            fields.put(smartInfo.getFieldType().name(), smartInfo.getPattern());
        }
        return fields;
    }
}