package de.codewave.mytunesrss.job;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * de.codewave.mytunesrss.job.MyTunesRssJobUtils
 */
public class MyTunesRssJobUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssJobUtils.class);
    private static final long MILLIS_PER_HOUR = 3600000;

    public static void scheduleStatisticEventsJob() {
        try {
            MyTunesRss.QUARTZ_SCHEDULER.unscheduleJob("RemoveOldEvents", "StatisticEvents");
            MyTunesRss.QUARTZ_SCHEDULER.addJob(new JobDetail(RemoveStatisticEventsJob.class.getSimpleName(), null, RemoveStatisticEventsJob.class),
                    true);
            Trigger trigger = new SimpleTrigger("RemoveOldEvents",
                    "StatisticEvents",
                    RemoveStatisticEventsJob.class.getSimpleName(),
                    null,
                    new Date(),
                    null,
                    SimpleTrigger.REPEAT_INDEFINITELY,
                    MILLIS_PER_HOUR);
            MyTunesRss.QUARTZ_SCHEDULER.scheduleJob(trigger);
        } catch (SchedulerException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not schedule statistic events removal job.", e);
            }
        }
    }

    /**
     * Schedule the database update job for all cron triggers in the configuration. Remove possibly existing triggers for that job first.
     */
    public static void scheduleDatabaseJob() {
        try {
            for (String trigger : MyTunesRss.QUARTZ_SCHEDULER.getTriggerNames("DatabaseUpdate")) {
                MyTunesRss.QUARTZ_SCHEDULER.unscheduleJob(trigger, "DatabaseUpdate");
            }
            MyTunesRss.QUARTZ_SCHEDULER.addJob(new JobDetail(DatabaseUpdateJob.class.getSimpleName(), null, DatabaseUpdateJob.class), true);
            for (String cronTrigger : MyTunesRss.CONFIG.getDatabaseUpdateTriggers()) {
                LOG.info("Adding database update trigger \"" + cronTrigger + "\".");
                try {
                    Trigger trigger = new CronTrigger("crontrigger[" + cronTrigger + "]",
                            "DatabaseUpdate",
                            DatabaseUpdateJob.class.getSimpleName(),
                            null,
                            cronTrigger);
                    MyTunesRss.QUARTZ_SCHEDULER.scheduleJob(trigger);
                } catch (ParseException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Could not schedule database update job for cron expression \"" + cronTrigger + "\".", e);
                    }
                } catch (SchedulerException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Could not schedule database update job for cron expression \"" + cronTrigger + "\".", e);
                    }
                }
            }
            for (String trigger : MyTunesRss.QUARTZ_SCHEDULER.getTriggerNames("DatabaseBackup")) {
                MyTunesRss.QUARTZ_SCHEDULER.unscheduleJob(trigger, "DatabaseBackup");
            }
            if (MyTunesRss.CONFIG.isDefaultDatabase()) {
                MyTunesRss.QUARTZ_SCHEDULER.addJob(new JobDetail(DatabaseBackupJob.class.getSimpleName(), null, DatabaseBackupJob.class), true);
                for (String cronTrigger : MyTunesRss.CONFIG.getDatabaseBackupTriggers()) {
                    LOG.info("Adding database backup trigger \"" + cronTrigger + "\".");
                    try {
                        Trigger trigger = new CronTrigger("crontrigger[" + cronTrigger + "]",
                                "DatabaseBackup",
                                DatabaseBackupJob.class.getSimpleName(),
                                null,
                                cronTrigger);
                        MyTunesRss.QUARTZ_SCHEDULER.scheduleJob(trigger);
                    } catch (ParseException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Could not schedule database backup job for cron expression \"" + cronTrigger + "\".", e);
                        }
                    } catch (SchedulerException e) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Could not schedule database backup job for cron expression \"" + cronTrigger + "\".", e);
                        }
                    }
                }
            }
        } catch (SchedulerException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not schedule database update or backup job.", e);
            }
        }
    }

    public static TriggerItem[] getDays() {
        String[] keys = new String[]{"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN", "MON-FRI", "SUN-SAT", "SAT,SUN"};
        TriggerItem[] items = new TriggerItem[keys.length];
        for (int i = 0; i < keys.length; i++) {
            items[i] = new TriggerItem(keys[i], MyTunesRssUtils.getBundleString(Locale.getDefault(), "settings.cron.day." + keys[i]));
        }
        return items;
    }

    public static TriggerItem[] getHours() {
        TriggerItem[] values = new TriggerItem[29];
        values[24] = new TriggerItem("0/1", "00/01");
        values[25] = new TriggerItem("0/2", "00/02");
        values[26] = new TriggerItem("0/3", "00/03");
        values[27] = new TriggerItem("0/6", "00/06");
        values[28] = new TriggerItem("0/12", "00/12");
        for (int i = 0; i < 24; i++) {
            String key = Integer.toString(i);
            String value = (i < 10 ? "0" : "") + Integer.toString(i);
            values[i] = new TriggerItem(key, value);
        }
        return values;
    }

    public static TriggerItem[] getMinutes() {
        TriggerItem[] values = new TriggerItem[18];
        values[12] = new TriggerItem("0/1", "00/01");
        values[13] = new TriggerItem("0/5", "00/05");
        values[14] = new TriggerItem("0/10", "00/10");
        values[15] = new TriggerItem("0/15", "00/15");
        values[16] = new TriggerItem("0/20", "00/20");
        values[17] = new TriggerItem("0/30", "00/30");
        for (int i = 0; i < 60; i += 5) {
            String key = Integer.toString(i);
            String value = (i < 10 ? "0" : "") + Integer.toString(i);
            values[i / 5] = new TriggerItem(key, value);
        }
        return values;
    }

    public static class TriggerItem {
        private String myKey;
        private String myValue;

        public TriggerItem(String key, String value) {
            myKey = key;
            myValue = value;
        }

        public String getKey() {
            return myKey;
        }

        @Override
        public String toString() {
            return myValue;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof TriggerItem && myKey.equals(((TriggerItem) obj).myKey);
        }

        @Override
        public int hashCode() {
            return myKey.hashCode();
        }
    }
}