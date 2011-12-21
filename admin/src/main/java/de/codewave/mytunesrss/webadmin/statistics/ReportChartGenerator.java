/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public interface ReportChartGenerator {
    JFreeChart generate(Map<Day, List<StatisticsEvent>> eventsPerDay, ResourceBundle bundle);
    StatEventType[] getEventTypes();
}
