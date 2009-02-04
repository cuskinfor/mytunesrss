package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.statistics.GetStatisticEventsQuery;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import javax.management.NotCompliantMBeanException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Statistic config mbean
 */
public class StatisticConfig extends MyTunesRssMBean implements StatisticConfigMBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticConfig.class);


    StatisticConfig() throws NotCompliantMBeanException {
        super(StatisticConfigMBean.class);
    }

    public int getEventsKeepDays() {
        return MyTunesRss.CONFIG.getStatisticKeepTime();
    }

    public void setEventsKeepDays(int days) {
        MyTunesRss.CONFIG.setStatisticKeepTime(days);
        onChange();
    }

    public String sendAdminEmail(String fromDate, String toDate) {
        if (StringUtils.isBlank(MyTunesRss.CONFIG.getAdminEmail())) {
            return MyTunesRssUtils.getBundleString("error.statisticsMissingAdminEmail");
        } else if (!MyTunesRss.CONFIG.isValidMailConfig()) {
            return MyTunesRssUtils.getBundleString("error.statisticsMissingEmailConfig");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(MyTunesRssUtils.getBundleString("common.dateFormat"));
        sdf.setLenient(false);
        Calendar from = new GregorianCalendar();
        Calendar to = new GregorianCalendar();
        try {
            from.setTime(sdf.parse(fromDate));
        } catch (ParseException e) {
            return MyTunesRssUtils.getBundleString("error.jmx.statistic.couldNotParseFromDate");
        }
        try {
            to.setTime(sdf.parse(toDate));
        } catch (ParseException e) {
            return MyTunesRssUtils.getBundleString("error.jmx.statistic.couldNotParseToDate");
        }
        return sendAdminStatistics(from, to);
    }

    private String sendAdminStatistics(Calendar from, Calendar to) {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            tx.executeStatement(new RemoveOldEventsStatement());
            Calendar nextDay = new GregorianCalendar();
            nextDay.setTimeInMillis(to.getTimeInMillis());
            nextDay.add(Calendar.DATE, 1);
            List<String> csv = tx.executeQuery(new GetStatisticEventsQuery(from.getTimeInMillis(), nextDay.getTimeInMillis(), "yyyy-MM-dd"));
            tx.commit();
            String statisticsMailBody = StringUtils.join(csv, System.getProperty("line.separator"));
            try {
                MyTunesRss.MAILER.sendMail(MyTunesRss.CONFIG.getAdminEmail(), MyTunesRssUtils.getBundleString("email.subject.statistics",
                                                                                                              Integer.toString(from.get(Calendar.DAY_OF_MONTH)),
                                                                                                              Integer.toString(
                                                                                                                      from.get(Calendar.MONTH) + 1),
                                                                                                              Integer.toString(from.get(Calendar.YEAR)),
                                                                                                              Integer.toString(to.get(Calendar.DAY_OF_MONTH)),
                                                                                                              Integer.toString(
                                                                                                                      to.get(Calendar.MONTH) + 1),
                                                                                                              Integer.toString(to.get(Calendar.YEAR))),
                                           statisticsMailBody);
                return MyTunesRssUtils.getBundleString("info.sendStatisticsDone");
            } catch (MailException e) {
                LOGGER.error("Could not send admin statstics email.", e);
                return MyTunesRssUtils.getBundleString("error.couldNotSendMail", e.getMessage());
            }
        } catch (final SQLException e) {
            try {
                tx.rollback();
            } catch (SQLException e1) {
                LOGGER.error("Could not rollback transaction.", e1);
            }
            return MyTunesRssUtils.getBundleString("error.couldNotSendStatistics", e.getMessage());
        }
    }
}