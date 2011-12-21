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
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DownVolumePerDayChartGenerator implements ReportChartGenerator {
    public JFreeChart generate(Map<Day, List<StatisticsEvent>> eventsPerDay, ResourceBundle bundle) {
        TimeSeries ts = new TimeSeries(getClass().getSimpleName());
        for (Map.Entry<Day, List<StatisticsEvent>> entry : eventsPerDay.entrySet()) {
            long volume = 0;
            for (StatisticsEvent event : entry.getValue()) {
                volume += ((DownloadEvent)event).myBytes;
            }
            ts.add(entry.getKey(), volume / (1024 * 1024)); // megabyte
        }
        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection(ts);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(bundle.getString(toString()), bundle.getString("statisticsConfigPanel.chart.axisDate"), bundle.getString("statisticsConfigPanel.chart.axisVolumeMib"), timeSeriesCollection, false, true, false);
        ((DateAxis)chart.getXYPlot().getDomainAxis()).setTickUnit(new DateTickUnit(DateTickUnitType.DAY, Math.max(1, eventsPerDay.size() / 10)));
        return chart;
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
