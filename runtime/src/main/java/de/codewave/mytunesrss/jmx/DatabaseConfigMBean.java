/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.DatabaseConfigMBean
 */
public interface DatabaseConfigMBean {
    String resetDatabase();

    String updateDatabase();

    String getDatabaseStatus();

    boolean isUpdateOnServerStart();

    void setUpdateOnServerStart(boolean updateOnServerStart);

    String[] getSchedules();

    String addSchedule(String schedule);

    String removeSchedule(int index);

    String[] getStatistics();

    boolean isRemoveMissingItunesTracks();

    void setRemoveMissingItunesTracks(boolean removeMissingTracks);

    String getDatabaseConnection();

    void setDatabaseConnection(String databaseConnection);

    String getDatabaseDriver();

    void setDatabaseDriver(String databaseDriver);

    String getDatabasePassword();

    void setDatabasePassword(String databasePassword);

    String getDatabaseType();

    void setDatabaseType(String databaseType);

    String getDatabaseUser();

    void setDatabaseUser(String databaseUsername);
}