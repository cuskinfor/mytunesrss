/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import org.apache.commons.lang.mutable.MutableLong;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;

import java.sql.SQLException;
import java.util.*;

public abstract class TopChartGenerator implements ReportChartGenerator {

    private static final int MAX_SLICES = 10;

    public JFreeChart generate(Map<Day, List<StatisticsEvent>> eventsPerDay, ResourceBundle bundle) throws SQLException {
        final Map<String, MutableLong> itemsWithCount = getItemsWithCount(eventsPerDay);
        List<String> items = new ArrayList<String>(itemsWithCount.keySet());
        Collections.sort(items, new Comparator<String>() {
            public int compare(String id1, String id2) {
                return itemsWithCount.get(id2).compareTo(itemsWithCount.get(id1));
            }
        });
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (String item : items.subList(0, Math.min(MAX_SLICES, items.size()))) {
            MutableLong count = itemsWithCount.get(item);
            pieDataset.setValue(getItemLabel(item, count.longValue()), count);
        }
        return ChartFactory.createPieChart(bundle.getString(toString()), pieDataset, false, true, false);
    }

    protected String getItemLabel(String item, long count) {
        return item + " = " + count;
    }

    protected abstract Map<String, MutableLong> getItemsWithCount(Map<Day, List<StatisticsEvent>> eventsPerDay) throws SQLException;

    protected abstract String getItem(Track track);
}
