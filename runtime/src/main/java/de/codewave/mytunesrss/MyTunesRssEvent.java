/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

/**
 * de.codewave.mytunesrss.MyTunesRssEvent
 */
public enum MyTunesRssEvent {
    ENABLE_AUTO_START_SERVER,
    DISABLE_AUTO_START_SERVER,
    CONFIGURATION_CHANGED, SERVER_STOPPED, SERVER_STARTED, DATABASE_UPDATE_FINISHED, DATABASE_UPDATE_FINISHED_NOT_RUN,
    DATABASE_UPDATE_STATE_CHANGED;

    private String myMessageKey;

    public void setMessageKey(String messageKey) {
        myMessageKey = messageKey;
    }

    public String getMessageKey() {
        return myMessageKey;
    }
}