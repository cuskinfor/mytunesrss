/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jfree.data.time.Day;

import java.sql.SQLException;
import java.util.*;

public abstract class TopTrackDownloadChartGenerator extends TopChartGenerator {

    @Override
    public StatEventType[] getEventTypes() {
        return new StatEventType[]{
                StatEventType.DOWNLOAD
        };
    }

    protected List<Track> getFlatTrackList(Map<Day, List<StatisticsEvent>> eventsPerDay) throws SQLException {
        List<String> trackIds = new ArrayList<>();
        for (List<StatisticsEvent> eventList : eventsPerDay.values()) {
            for (StatisticsEvent event : eventList) {
                if (((DownloadEvent)event).myTrackId != null) {
                    trackIds.add(((DownloadEvent)event).myTrackId);
                }
            }
        }
        Set<String> uniqueTrackIds = new HashSet<>(trackIds);
        Map<String, Track> trackMapping = new HashMap<>();
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            for (Track track : tx.executeQuery(FindTrackQuery.getForIds(uniqueTrackIds.toArray(new String[uniqueTrackIds.size()]))).getResults()) {
                trackMapping.put(track.getId(), track);
            }
        } finally {
            tx.rollback();
        }
        List<Track> tracks = new ArrayList<>();
        for (String trackId : trackIds) {
            tracks.add(trackMapping.get(trackId));
        }
        return tracks;
    }

    @Override
    protected Map<String, MutableLong> getItemsWithCount(Map<Day, List<StatisticsEvent>> eventsPerDay) throws SQLException {
        Map<String, MutableLong> items = new HashMap<>();
        for (Track track : getFlatTrackList(eventsPerDay)) {
            if (track != null) {
                String item = getItem(track);
                if (StringUtils.isNotBlank(item)) {
                    if (items.containsKey(item)) {
                        items.get(item).add(1);
                    } else {
                        items.put(item, new MutableLong(1));
                    }
                }
            }
        }
        return items;
    }

    protected abstract String getItem(Track track);
}
