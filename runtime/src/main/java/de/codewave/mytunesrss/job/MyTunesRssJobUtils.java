package de.codewave.mytunesrss.job;

import org.quartz.*;
import org.apache.commons.logging.*;
import de.codewave.mytunesrss.*;

import java.text.*;

/**
 * de.codewave.mytunesrss.job.MyTunesRssJobUtils
 */
public class MyTunesRssJobUtils {
    private static final Log LOG = LogFactory.getLog(MyTunesRssJobUtils.class);

    /**
     * Schedule the database update job for all cron triggers in the configuration. Remove possibly existing triggers for that job first.
     */
    public static void scheduleDatabaseJob() {
        try {
            for (String trigger : MyTunesRss.QUARTZ_SCHEDULER.getTriggerNames("DatabaseUpdate")) {
                MyTunesRss.QUARTZ_SCHEDULER.unscheduleJob(trigger, "DatabaseUpdate");
            }
            MyTunesRss.QUARTZ_SCHEDULER.addJob(new JobDetail(DatabaseUpdateJob.class.getSimpleName(), null, DatabaseUpdateJob.class), true);
            for (String cronTrigger : MyTunesRss.CONFIG.getDatabaseCronTriggers()) {
                try {
                    Trigger trigger = new CronTrigger("crontrigger[" + cronTrigger + "]",
                                                      "DatabaseUpdate", DatabaseUpdateJob.class.getSimpleName(), null, cronTrigger);
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
        } catch (SchedulerException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not schedule database update job.", e);
            }
        }
    }
}