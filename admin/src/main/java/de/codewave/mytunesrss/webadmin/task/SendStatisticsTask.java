/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.task;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.statistics.GetStatisticEventsQuery;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.vaadin.component.ProgressWindow;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class SendStatisticsTask implements ProgressWindow.Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendStatisticsTask.class);

    private MainWindow myMainWindow;

    private Calendar myFrom;

    private Calendar myTo;

    private String myError;

    private String myInfo;

    public SendStatisticsTask(MainWindow mainWindow, Calendar from, Calendar to) {
        myMainWindow = mainWindow;
        myFrom = from;
        myTo = to;
    }

    public int getProgress() {
        return 0;
    }

    public void onWindowClosed() {
        if (myError != null) {
            myMainWindow.showInfo(myError);
        } else if (myInfo != null) {
            myMainWindow.showInfo(myInfo);
        }
    }

    public void run() {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            tx.executeStatement(new RemoveOldEventsStatement());
            Calendar nextDay = new GregorianCalendar();
            nextDay.setTimeInMillis(myTo.getTimeInMillis());
            nextDay.add(Calendar.DATE, 1);
            List<String> csv = tx.executeQuery(new GetStatisticEventsQuery(myFrom.getTimeInMillis(), nextDay.getTimeInMillis(), "yyyy-MM-dd"));
            tx.commit();
            String statisticsMailBody = StringUtils.join(csv, System.getProperty("line.separator"));
            try {
                MyTunesRss.MAILER.sendMail(MyTunesRss.CONFIG.getAdminEmail(), MyTunesRssUtils.getBundleString(Locale.getDefault(), "email.subject.statistics",
                        Integer.toString(myFrom.get(Calendar.DAY_OF_MONTH)),
                        Integer.toString(myFrom.get(Calendar.MONTH) + 1),
                        Integer.toString(myFrom.get(Calendar.YEAR)),
                        Integer.toString(myTo.get(Calendar.DAY_OF_MONTH)),
                        Integer.toString(myTo.get(Calendar.MONTH) + 1),
                        Integer.toString(myTo.get(Calendar.YEAR))),
                        statisticsMailBody);
                myInfo = "statisticsConfigPanel.info.statisticsSentSuccessfully";
            } catch (final MailException e) {
                LOGGER.error("Could not send statistics email.", e);
                myError = "statisticsConfigPanel.error.statisticsNotSent";
            }
        } catch (final SQLException e) {
            try {
                tx.rollback();
            } catch (SQLException e1) {
                LOGGER.error("Could not rollback transaction.", e1);
            }
            myError = "statisticsConfigPanel.error.statisticsNotSent";
        }
    }
}
