package de.codewave.mytunesrss.statistics;

import java.io.IOException;

public interface StatisticsEvent {
    StatEventType getType();

    long getEventTime();

    String toJson() throws IOException;
}
