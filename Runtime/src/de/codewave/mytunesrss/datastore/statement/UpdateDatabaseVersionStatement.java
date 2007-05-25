package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class UpdateDatabaseVersionStatement implements DataStoreStatement {
    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updateDatabaseVersion");
        statement.setString("version", MyTunesRss.VERSION);
        statement.execute();
    }
}