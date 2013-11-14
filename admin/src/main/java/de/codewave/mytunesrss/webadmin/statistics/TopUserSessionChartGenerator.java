/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.statistics.SessionEndEvent;
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

public class TopUserSessionChartGenerator extends TopChartGenerator {

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.topUserSession";
    }

    @Override
    protected Map<String, MutableLong> getItemsWithCount(Map<Day, List<StatisticsEvent>> eventsPerDay) throws SQLException {
        Map<String, MutableLong> itemsWithCount = new HashMap<String, MutableLong>();
        for (List<StatisticsEvent> eventList : eventsPerDay.values()) {
            for (StatisticsEvent event : eventList) {
                String user = ((SessionEndEvent)event).myUser;
                long duration = ((SessionEndEvent)event).myDuration;
                if (itemsWithCount.containsKey(user)) {
                    itemsWithCount.get(user).add(duration / 1000);
                } else {
                    itemsWithCount.put(user, new MutableLong(duration / 1000));
                }
            }
        }
        return itemsWithCount;
    }

    @Override
    protected String getItemLabel(String item, long seconds, ResourceBundle bundle) {
        long h = seconds / 3600;
        long m = (seconds - (3600 * h)) / 60;
        long s = seconds % 60;
        DecimalFormat df = new DecimalFormat("00");
        return item + " = " + h + ":" + df.format(m) + ":" + df.format(s);
    }

    public StatEventType[] getEventTypes() {
        return new StatEventType[]{
                StatEventType.SESSION_END
        };
    }
}
