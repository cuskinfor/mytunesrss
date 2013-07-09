package de.codewave.mytunesrss.datastore.statement;

import java.util.*;

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

    public Collection<SmartInfo> getSmartInfosSortedForWebForm() {
        List<SmartInfo> sorted = new ArrayList<SmartInfo>(mySmartInfos);
        Collections.sort(sorted, new Comparator<SmartInfo>() {
            public int compare(SmartInfo o1, SmartInfo o2) {
                int v1 = 0;
                int v2 = 0;
                switch (o1.getFieldType()) {
                    case sizeLimit: // v1 -> 2
                        v1++;
                    case randomOrder: // v1 -> 1
                        v1++;                       
                }
                switch (o2.getFieldType()) {
                    case sizeLimit: // v2 -> 2
                        v2++;
                    case randomOrder: // v2 -> 1
                        v2++;                       
                }
                return v1 - v2;
            }
        });
        return sorted;
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
