package de.codewave.mytunesrss.webadmin.statistics;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class TimeSeriesCharGenerator {
    protected JFreeChart createTimeSeriesChart(TimeSeriesCollection timeSeriesCollection, ResourceBundle bundle, String rangeAxisBundleKey) {
        long maxValue = Long.MIN_VALUE;
        for (TimeSeries series : (Iterable<TimeSeries>)timeSeriesCollection.getSeries()) {
            for (TimeSeriesDataItem item : (Iterable<TimeSeriesDataItem>)series.getItems()) {
                maxValue = Math.max(maxValue, item.getValue().longValue());
            }
        }
        JFreeChart chart = ChartFactory.createTimeSeriesChart(bundle.getString(toString()), bundle.getString("statisticsConfigPanel.chart.axisDate"), bundle.getString(rangeAxisBundleKey), timeSeriesCollection, timeSeriesCollection.getSeries().size() > 1, true, false);
        ((DateAxis)chart.getXYPlot().getDomainAxis()).setTickUnit(new DateTickUnit(DateTickUnitType.DAY, Math.max(1, timeSeriesCollection.getSeries(0).getItemCount() / 10)));
        chart.getXYPlot().getRangeAxis().setLowerBound(0);
        ((NumberAxis)chart.getXYPlot().getRangeAxis()).setTickUnit(new NumberTickUnit(Math.max(1, maxValue / 10), new DecimalFormat("#")));
        return chart;
    }

}
