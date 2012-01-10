package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.statistics.InsertStatisticsStatement
 */
public class InsertStatisticsEventStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertStatisticsEventStatement.class);

    private StatisticsEvent myEvent;

    public InsertStatisticsEventStatement(StatisticsEvent event) {
        myEvent = event;
    }

    public void execute(Connection connection) throws SQLException {
        String json;
        try {
            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "insertStatisticsEvent");
            statement.setObject("ts", myEvent.getEventTime());
            statement.setObject("type", myEvent.getType().getValue());
            statement.setObject("data", myEvent.toJson());
            statement.execute();
        } catch (IOException e) {
            LOGGER.warn("Could not write statistics event of type \"" + myEvent.getType().name() + "\".", e);
        }
    }

}