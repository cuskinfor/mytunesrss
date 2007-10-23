/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.DatabaseConfigMBean
 */
public interface DatabaseConfigMBean {
    boolean isIgnoreTimestampsOnUpdate();

    void setIgnoreTimestampsOnUpdate(boolean ignoreTimestamps);

    String resetDatabase();

    String updateDatabase();

    String getDatabaseStatus();

    boolean isUpdateOnServerStart();

    void setUpdateOnServerStart(boolean updateOnServerStart);

    boolean isAutoUpdate();

    void setAutoUpdate(boolean autoUpdate);

    int getAutoUpdateIntervalMinutes();

    void setAutoUpdateIntervalMinutes(int minutes);

    String getFileTypes();

    void setFileTypes(String fileTypes);

    String getArtistDropWords();

    void setArtistDropWords(String artistDropWords);

    boolean isRemoveMissingItunesTracks();

    void setRemoveMissingItunesTracks(boolean removeMissingTracks);
}