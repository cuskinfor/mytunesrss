package de.codewave.mytunesrss.statistics;

public class NullEvent extends AbstractEvent {

    public static final NullEvent INSTANCE = new NullEvent();

    public NullEvent() {
        super(StatEventType.NULL);
        setEventTime(Long.MIN_VALUE);
    }
}
