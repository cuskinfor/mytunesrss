package de.codewave.mytunesrss.statistics;

import javax.xml.bind.annotation.XmlTransient;

public class NullEvent extends AbstractEvent {

    public static final NullEvent INSTANCE = new NullEvent();

    public NullEvent() {
        super(StatEventType.NULL);
    }
}
