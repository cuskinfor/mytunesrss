/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jfree.data.time.Day;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TopUserDownVolumeChartGenerator extends TopChartGenerator {

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.topUserDownVolume";
    }

    @Override
    protected Map<String, MutableLong> getItemsWithCount(Map<Day, List<StatisticsEvent>> eventsPerDay) throws SQLException {
        Map<String, MutableLong> itemsWithCount = new HashMap<>();
        for (List<StatisticsEvent> eventList : eventsPerDay.values()) {
            for (StatisticsEvent event : eventList) {
                String user = ((DownloadEvent)event).myUser;
                long bytes = ((DownloadEvent)event).myBytes;
                if (itemsWithCount.containsKey(user)) {
                    itemsWithCount.get(user).add(bytes / 1024);
                } else {
                    itemsWithCount.put(user, new MutableLong(bytes / 1024));
                }
            }
        }
        return itemsWithCount;
    }

    @Override
    protected String getItemLabel(String item, long value, ResourceBundle bundle) {
        long gib = value / (1024 * 1024);
        long mib = (value - (1024 * 1024 * gib)) / 1024;
        long kib = value % 1024;
        DecimalFormat decimalFormat = new DecimalFormat("000");
        if (gib > 0) {
            return item + " = " + gib + bundle.getString("statisticsConfigPanel.chart.decimalSeparator") + decimalFormat.format(mib) + " GiB";
        } else if (mib > 0) {
            return item + " = " + mib + bundle.getString("statisticsConfigPanel.chart.decimalSeparator") + decimalFormat.format(kib) + " MiB";
        } else {
            return item + " = " + kib + " KiB";
        }
    }

    @Override
    public StatEventType[] getEventTypes() {
        return new StatEventType[]{
                StatEventType.DOWNLOAD
        };
    }
}
