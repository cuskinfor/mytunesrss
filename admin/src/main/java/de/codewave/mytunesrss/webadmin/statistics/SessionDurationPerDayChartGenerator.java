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
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.util.*;

public class SessionDurationPerDayChartGenerator implements ReportChartGenerator {
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
        JFreeChart chart = ChartFactory.createTimeSeriesChart(bundle.getString(toString()), bundle.getString("statisticsConfigPanel.chart.axisDate"), bundle.getString("statisticsConfigPanel.chart.axisSessionDuration"), timeSeriesCollection, true, true, false);
        ((DateAxis)chart.getXYPlot().getDomainAxis()).setTickUnit(new DateTickUnit(DateTickUnitType.DAY, Math.max(1, eventsPerDay.size() / 10)));
        return chart;
    }

    private double getMin(List<StatisticsEvent> events) {
        long min = Long.MAX_VALUE;
        for (StatisticsEvent event : events) {
            min = Math.min(min, ((SessionEndEvent)event).myDuration);
        }
        return min;
    }

    private double getMax(List<StatisticsEvent> events) {
        long max = Long.MIN_VALUE;
        for (StatisticsEvent event : events) {
            max = Math.max(max, ((SessionEndEvent) event).myDuration);
        }
        return max;
    }

    private double getMedian(List<StatisticsEvent> events) {
        List<Long> durations = new ArrayList<Long>();
        for (StatisticsEvent event : events) {
            durations.add(Long.valueOf(((SessionEndEvent)event).myDuration));
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
