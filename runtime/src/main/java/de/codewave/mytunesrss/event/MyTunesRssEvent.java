/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.event;

/**
 * de.codewave.mytunesrss.event.MyTunesRssEvent
 */
public class MyTunesRssEvent {

    public enum EventType {
        SERVER_STOPPED, SERVER_STARTED, DATABASE_UPDATE_FINISHED, DATABASE_UPDATE_STATE_CHANGED, MYTUNESRSS_COM_UPDATED;
    }

    public static MyTunesRssEvent create(EventType type) {
        return new MyTunesRssEvent(type, null, null, null);
    }

    public static MyTunesRssEvent create(EventType type, String messageKey) {
        return new MyTunesRssEvent(type, messageKey, null, null);
    }

    private EventType myType;
    private String myMessageKey;
    private Object[] myMessageParams;
    private Object[] myEventParams;

    public MyTunesRssEvent(EventType type, String messageKey, Object[] messageParams, Object[] eventParams) {
        myType = type;
        myMessageKey = messageKey;
        myMessageParams = messageParams;
        myEventParams = eventParams;
    }

    public EventType getType() {
        return myType;
    }

    public void setMessageKey(String messageKey) {
        myMessageKey = messageKey;
    }

    public String getMessageKey() {
        return myMessageKey;
    }

    public Object[] getMessageParams() {
        return myMessageParams;
    }

    public void setMessageParams(Object... messageParams) {
        myMessageParams = messageParams;
    }

    public Object[] getEventParams() {
        return myEventParams;
    }

    public void setEventParams(Object[] eventParams) {
        myEventParams = eventParams;
    }
}