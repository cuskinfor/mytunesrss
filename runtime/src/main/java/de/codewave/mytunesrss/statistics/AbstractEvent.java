package de.codewave.mytunesrss.statistics;

public abstract class AbstractEvent implements StatisticsEvent {
    private long myEventTime = System.currentTimeMillis();

    public long getEventTime() {
        return myEventTime;
    }

    public void setEventTime(long eventTime) {
        myEventTime = eventTime;
    }
}
