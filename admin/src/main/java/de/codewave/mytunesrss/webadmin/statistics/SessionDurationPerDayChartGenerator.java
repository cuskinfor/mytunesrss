/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.statistics.SessionEndEvent;
import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.text.DecimalFormat;
import java.util.*;

public class SessionDurationPerDayChartGenerator extends TimeSeriesCharGenerator implements ReportChartGenerator {
    public JFreeChart generate(Map<Day, List<StatisticsEvent>> eventsPerDay, ResourceBundle bundle) {
        TimeSeries tsMin = new TimeSeries(bundle.getString("statisticsConfigPanel.chart.seriesMin"));
        TimeSeries tsMax = new TimeSeries(bundle.getString("statisticsConfigPanel.chart.seriesMax"));
        TimeSeries tsMedian = new TimeSeries(bundle.getString("statisticsConfigPanel.chart.seriesMedian"));
        for (Map.Entry<Day, List<StatisticsEvent>> entry : eventsPerDay.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                tsMin.add(entry.getKey(), getMin(entry.getValue()));
                tsMax.add(entry.getKey(), getMax(entry.getValue()));
                tsMedian.add(entry.getKey(), getMedian(entry.getValue()));
            } else {
                tsMin.add(entry.getKey(), 0);
                tsMax.add(entry.getKey(), 0);
                tsMedian.add(entry.getKey(), 0);
            }
        }
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection(tsMin);
        timeSeriesCollection.addSeries(tsMax);
        timeSeriesCollection.addSeries(tsMedian);
        return createTimeSeriesChart(timeSeriesCollection, bundle, "statisticsConfigPanel.chart.axisSessionDuration");
    }

    private long getMin(List<StatisticsEvent> events) {
        long min = Long.MAX_VALUE;
        for (StatisticsEvent event : events) {
            min = Math.min(min, (((SessionEndEvent)event).myDuration / 1000)); // seconds
        }
        return min;
    }

    private long getMax(List<StatisticsEvent> events) {
        long max = Long.MIN_VALUE;
        for (StatisticsEvent event : events) {
            max = Math.max(max, (((SessionEndEvent)event).myDuration / 1000)); // seconds
        }
        return max;
    }

    private long getMedian(List<StatisticsEvent> events) {
        List<Long> durations = new ArrayList<Long>();
        for (StatisticsEvent event : events) {
            durations.add(Long.valueOf(((SessionEndEvent)event).myDuration / 1000)); // seconds
        }
        Collections.sort(durations);
        return durations.get(durations.size() / 2);
    }

    public StatEventType[] getEventTypes() {
        return new StatEventType[] {
                StatEventType.SESSION_END
        };
    }

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.sessionDurationPerDay";
    }
}
