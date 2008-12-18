package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.statistics.InsertStatisticsStatement
 */
public class RemoveOldEventsStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOldEventsStatement.class);

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeOldStatisticEvents");
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.DATE, -MyTunesRss.CONFIG.getStatisticKeepTime());
        statement.setObject("ts", calendar.getTimeInMillis());
        LOGGER.debug("Removing statistics with timestamp less than " + calendar.getTimeInMillis());
        statement.execute();
    }
}