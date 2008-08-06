/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.GetSystemInformationQuery;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import de.codewave.mytunesrss.job.MyTunesRssJobUtils;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.mytunesrss.task.RecreateDatabaseTask;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotCompliantMBeanException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * de.codewave.mytunesrss.jmx.DatabaseConfig
 */
public class DatabaseConfig extends MyTunesRssMBean implements DatabaseConfigMBean, MyTunesRssEventListener {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfig.class);

    private String myCurrentUpdateAction;

    DatabaseConfig() throws NotCompliantMBeanException {
        super(DatabaseConfigMBean.class);
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public boolean isIgnoreTimestampsOnUpdate() {
        return MyTunesRss.CONFIG.isIgnoreTimestamps();
    }

    public void setIgnoreTimestampsOnUpdate(boolean ignoreTimestamps) {
        MyTunesRss.CONFIG.setIgnoreTimestamps(ignoreTimestamps);
        onChange();
    }

    public String resetDatabase() {
        MyTunesRssUtils.executeTask(null, null, null, false, new RecreateDatabaseTask());
        onChange();
        return MyTunesRss.ERROR_QUEUE.popLastError();
    }

    public synchronized String updateDatabase() {
        if (!DatabaseBuilderTask.isRunning()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        final DatabaseBuilderTask databaseBuilderTask = new DatabaseBuilderTask();
                        databaseBuilderTask.execute();
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Error during database update", e);
                        }
                    }
                }
            }).start();
            onChange();
            return MyTunesRssUtils.getBundleString("jmx.databaseUpdateStarted");
        } else {
            return MyTunesRssUtils.getBundleString("jmx.databaseUpdateAlreadyRunning");
        }
    }

    public String getDatabaseStatus() {
        if (myCurrentUpdateAction != null) {
            return myCurrentUpdateAction;
        }
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            SystemInformation systemInformation = session.executeQuery(new GetSystemInformationQuery());
            if (systemInformation.getLastUpdate() > 0) {
                Date date = new Date(systemInformation.getLastUpdate());
                return MyTunesRssUtils.getBundleString("settings.lastDatabaseUpdate") + " " + new SimpleDateFormat(MyTunesRssUtils.getBundleString(
                        "settings.lastDatabaseUpdateDateFormat")).format(date);
            } else {
                return MyTunesRssUtils.getBundleString("settings.databaseNotYetCreated");
            }
        } catch (SQLException e) {
            return e.getMessage();
        } finally {
            session.commit();
        }
    }

    public boolean isUpdateOnServerStart() {
        return MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart();
    }

    public void setUpdateOnServerStart(boolean updateOnServerStart) {
        MyTunesRss.CONFIG.setUpdateDatabaseOnServerStart(updateOnServerStart);
        onChange();
    }

    public String getArtistDropWords() {
        return MyTunesRss.CONFIG.getArtistDropWords();
    }

    public void setArtistDropWords(String artistDropWords) {
        MyTunesRss.CONFIG.setArtistDropWords(artistDropWords);
        onChange();
    }

    public boolean isRemoveMissingItunesTracks() {
        return MyTunesRss.CONFIG.isItunesDeleteMissingFiles();
    }

    public void setRemoveMissingItunesTracks(boolean removeMissingTracks) {
        MyTunesRss.CONFIG.setItunesDeleteMissingFiles(removeMissingTracks);
        onChange();
    }

    public void handleEvent(MyTunesRssEvent event) {
        if (event == MyTunesRssEvent.DATABASE_UPDATE_STATE_CHANGED) {
            myCurrentUpdateAction = MyTunesRssUtils.getBundleString(event.getMessageKey());
        } else if (event == MyTunesRssEvent.DATABASE_UPDATE_FINISHED || event == MyTunesRssEvent.DATABASE_UPDATE_FINISHED_NOT_RUN) {
            myCurrentUpdateAction = null;
        }
    }

    public boolean isIgnoreCoverArtworkFromFiles() {
        return MyTunesRss.CONFIG.isIgnoreArtwork();
    }

    public void setIgnoreCoverArtworkFromFiles(boolean ignoreCoverArtwork) {
        MyTunesRss.CONFIG.setIgnoreArtwork(ignoreCoverArtwork);
        onChange();
    }

    public String addSchedule(String schedule) {
        String technicalSchedule = getTechnicalSchedule(schedule);
        if (technicalSchedule != null) {
            MyTunesRss.CONFIG.getDatabaseCronTriggers().add(technicalSchedule);
            MyTunesRssJobUtils.scheduleDatabaseJob();
            onChange();
            return MyTunesRssUtils.getBundleString("jmx.databaseScheduleAdded", schedule);
        } else {
            return MyTunesRssUtils.getBundleString("jmx.databaseScheduleInvalid", schedule);
        }
    }

    public String[] getSchedules() {
        String[] schedules = new String[MyTunesRss.CONFIG.getDatabaseCronTriggers().size()];
        for (int i = 0; i < schedules.length && i < MyTunesRss.CONFIG.getDatabaseCronTriggers().size(); i++) {
            schedules[i] = getDisplaySchedule(MyTunesRss.CONFIG.getDatabaseCronTriggers().get(i));
        }
        return schedules;
    }

    private String getDisplaySchedule(String technicalSchedule) {
        String[] tokens = technicalSchedule.split(" ");
        return tokens[5] + " " + tokens[2] + " " + tokens[1];
    }

    public String removeSchedule(int index) {
        if (MyTunesRss.CONFIG.getDatabaseCronTriggers().size() > index) {
            String schedule = MyTunesRss.CONFIG.getDatabaseCronTriggers().remove(index);
            MyTunesRssJobUtils.scheduleDatabaseJob();
            onChange();
            return MyTunesRssUtils.getBundleString("jmx.databaseScheduleRemoved", getDisplaySchedule(schedule));
        } else {
            return MyTunesRssUtils.getBundleString("jmx.noSuchDatabaseScheduleToRemove", index);
        }
    }

    private String getTechnicalSchedule(String schedule) {
        String[] tokens = schedule.split(" ");
        if (tokens.length == 3) {
            tokens[0] = getCorrectTechnicalToken(MyTunesRssJobUtils.getDays(), tokens[0]);
            tokens[1] = getCorrectTechnicalToken(MyTunesRssJobUtils.getHours(), tokens[1]);
            tokens[2] = getCorrectTechnicalToken(MyTunesRssJobUtils.getMinutes(), tokens[2]);
            if (tokens[0] != null && tokens[1] != null && tokens[2] != null) {
                return "* " + tokens[2] + " " + tokens[1] + " ? * " + tokens[0];
            }
        }
        return null;
    }

    private String getCorrectTechnicalToken(MyTunesRssJobUtils.TriggerItem[] items, String sample) {
        for (MyTunesRssJobUtils.TriggerItem item : items) {
            if (sample.trim().equalsIgnoreCase(item.getKey()) || sample.trim().equalsIgnoreCase(item.toString())) {
                return item.getKey();
            }
        }
        return null;
    }

    public String[] getStatistics() {
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            SystemInformation systemInformation = session.executeQuery(new GetSystemInformationQuery());
            if (systemInformation.getLastUpdate() > 0) {
                String[] statistics = new String[6];
                Date date = new Date(systemInformation.getLastUpdate());
                statistics[0] = MyTunesRssUtils.getBundleString("settings.lastDatabaseUpdate") + " " + new SimpleDateFormat(MyTunesRssUtils.getBundleString(
                        "settings.lastDatabaseUpdateDateFormat")).format(date);
                statistics[1] = MyTunesRssUtils.getBundleString("dbstat.version", systemInformation.getVersion());
                statistics[2] = MyTunesRssUtils.getBundleString("dbstat.tracks", systemInformation.getTrackCount());
                statistics[3] = MyTunesRssUtils.getBundleString("dbstat.albums", systemInformation.getAlbumCount());
                statistics[4] = MyTunesRssUtils.getBundleString("dbstat.artists", systemInformation.getArtistCount());
                statistics[5] = MyTunesRssUtils.getBundleString("dbstat.genres", systemInformation.getGenreCount());
                return statistics;
            } else {
                return new String[] {MyTunesRssUtils.getBundleString("settings.databaseNotYetCreated")};
            }
        } catch (SQLException e) {
            return new String[] {e.getMessage()};
        } finally {
            session.commit();
        }
    }
}