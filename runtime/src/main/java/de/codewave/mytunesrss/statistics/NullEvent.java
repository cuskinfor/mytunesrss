package de.codewave.mytunesrss.statistics;

public class NullEvent extends AbstractEvent {

    public static final NullEvent INSTANCE = new NullEvent();

    public StatEventType getType() {
        return StatEventType.NULL;
    }
}
