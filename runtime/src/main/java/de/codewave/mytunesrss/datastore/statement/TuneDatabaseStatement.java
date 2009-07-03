package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.TuneDatabaseStatement
 */
public class TuneDatabaseStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(TuneDatabaseStatement.class);

    public void execute(Connection connection) throws SQLException {
        LOGGER.info("Tuning database!");
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "tuneDatabase");
        statement.execute();
    }
}