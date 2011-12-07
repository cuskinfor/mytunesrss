package de.codewave.mytunesrss.statistics;

public enum StatEventType {
    DOWNLOAD(1, DownloadEvent.class),
    LOGIN(2, LoginEvent.class),
    UPLOAD(3, UploadEvent.class);

    private int myValue;
    private Class myClazz;

    private StatEventType(int value, Class clazz) {
        myValue = value;
        myClazz = clazz;
    }

    public int getValue() {
        return myValue;
    }

    public Class getEventClass(int typeValue) {
        for (StatEventType type : StatEventType.values()) {
            if (type.getValue() == typeValue) {
                return type.getClass();
            }
        }
        throw new IllegalArgumentException("Unknown event type value \"" + typeValue + "\".");
    }
}
