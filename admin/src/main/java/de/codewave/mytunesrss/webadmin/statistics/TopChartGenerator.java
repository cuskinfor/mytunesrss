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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableLong;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

public abstract class TopChartGenerator implements ReportChartGenerator {

    public JFreeChart generate(Map<Day, List<StatisticsEvent>> eventsPerDay, ResourceBundle bundle) throws SQLException {
        final Map<String, MutableLong> itemsWithCount = getItemsWithCount(getFlatTrackList(eventsPerDay));
        List<String> items = new ArrayList<String>(itemsWithCount.keySet());
        Collections.sort(items, new Comparator<String>() {
            public int compare(String id1, String id2) {
                return itemsWithCount.get(id2).compareTo(itemsWithCount.get(id1));
            }
        });
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (String item : items.subList(0, Math.min(10, items.size()))) {
            MutableLong count = itemsWithCount.get(item);
            pieDataset.setValue(item + " = " + count, count);
        }
        return ChartFactory.createPieChart(bundle.getString(toString()), pieDataset, false, true, false);
    }

    public StatEventType[] getEventTypes() {
        return new StatEventType[]{
                StatEventType.DOWNLOAD
        };
    }

    protected List<Track> getFlatTrackList(Map<Day, List<StatisticsEvent>> eventsPerDay) throws SQLException {
        List<String> trackIds = new ArrayList<String>();
        for (List<StatisticsEvent> eventList : eventsPerDay.values()) {
            for (StatisticsEvent event : eventList) {
                trackIds.add(((DownloadEvent)event).myTrackId);
            }
        }
        Set<String> uniqueTrackIds = new HashSet<String>(trackIds);
        Map<String, Track> trackMapping = new HashMap<String, Track>();
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            for (Track track : tx.executeQuery(FindTrackQuery.getForIds(uniqueTrackIds.toArray(new String[uniqueTrackIds.size()]))).getResults()) {
                trackMapping.put(track.getId(), track);
            }
        } finally {
            tx.rollback();
        }
        List<Track> tracks = new ArrayList<Track>();
        for (String trackId : trackIds) {
            tracks.add(trackMapping.get(trackId));
        }
        return tracks;
    }

    protected Map<String, MutableLong> getItemsWithCount(List<Track> tracks) {
        Map<String, MutableLong> items = new HashMap<String, MutableLong>();
        for (Track track : tracks) {
            String item = getItem(track);
            if (StringUtils.isNotBlank(item)) {
                if (items.containsKey(item)) {
                    items.get(item).add(1);
                } else {
                    items.put(item, new MutableLong(1));
                }
            }
        }
        return items;
    }

    protected abstract String getItem(Track track);
}
