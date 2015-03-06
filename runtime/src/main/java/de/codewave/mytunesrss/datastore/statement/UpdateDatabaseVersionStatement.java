package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.CreateAllTablesStatement
 */
public class UpdateDatabaseVersionStatement implements DataStoreStatement {
    private String myVersion;

    public UpdateDatabaseVersionStatement(String version) {
        myVersion = version;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updateDatabaseVersion");
        statement.setString("version", myVersion);
        statement.execute();
    }
}