/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.util.List;
import java.util.Map;

public class DownVolumePerDayChartGenerator implements ReportChartGenerator {
    public JFreeChart generate(Map<Day, List<StatisticsEvent>> eventsPerDay) {
        TimeSeries ts = new TimeSeries("@todo download volume");
        for (Map.Entry<Day, List<StatisticsEvent>> entry : eventsPerDay.entrySet()) {
            long volume = 0;
            for (StatisticsEvent event : entry.getValue()) {
                volume += ((DownloadEvent)event).myBytes;
            }
            ts.add(entry.getKey(), volume / (1024 * 1024)); // megabyte
        }
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection(ts);
        return ChartFactory.createTimeSeriesChart("@todo volume per day", "@todo date", "@todo volume [MiB]", timeSeriesCollection, false, true, false);
    }

    public StatEventType[] getEventTypes() {
        return new StatEventType[] {
                StatEventType.DOWNLOAD
        };
    }

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.downvolume";
    }
}
