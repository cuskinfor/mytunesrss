package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.TuneDatabaseStatement
 */
public class TuneDatabaseStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "tuneDatabase");
        statement.execute();
    }
}