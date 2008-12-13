package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.statistics.InsertStatisticsStatement
 */
public class InsertStatisticsEventStatement implements DataStoreStatement {
    private byte[] myData;

    public InsertStatisticsEventStatement(byte[] data) {
        myData = data;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "insertStatisticsEvent");
        statement.setObject("ts", System.currentTimeMillis());
        statement.setObject("data", myData);
        statement.execute();
    }
}