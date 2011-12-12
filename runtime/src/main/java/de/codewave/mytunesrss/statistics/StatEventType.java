package de.codewave.mytunesrss.statistics;

public enum StatEventType {
    NULL(0, NullEvent.class),
    DOWNLOAD(1, DownloadEvent.class),
    SESSION_START(2, SessionStartEvent.class),
    SESSION_END(3, SessionEndEvent.class),
    UPLOAD(4, UploadEvent.class);

    public static Class getEventClass(int typeValue) {
        for (StatEventType type : StatEventType.values()) {
            if (type.getValue() == typeValue) {
                return type.getClass();
            }
        }
        throw new IllegalArgumentException("Unknown event type value \"" + typeValue + "\".");
    }

    private int myValue;
    private Class myClazz;

    private StatEventType(int value, Class clazz) {
        myValue = value;
        myClazz = clazz;
    }

    public int getValue() {
        return myValue;
    }
}
