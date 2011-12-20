/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.util.List;
import java.util.Map;

public class SessionsPerDayChartGenerator implements ReportChartGenerator {
    public JFreeChart generate(Map<Day, List<StatisticsEvent>> eventsPerDay) {
        TimeSeries ts = new TimeSeries("@todo sessions");
        for (Map.Entry<Day, List<StatisticsEvent>> entry : eventsPerDay.entrySet()) {
            ts.add(entry.getKey(), entry.getValue().size());
        }
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection(ts);
        return ChartFactory.createTimeSeriesChart("@todo sessions per day", "@todo date", "@todo sessions", timeSeriesCollection, false, true, false);
    }

    public StatEventType[] getEventTypes() {
        return new StatEventType[] {
                StatEventType.SESSION_START
        };
    }

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.session";
    }
}
