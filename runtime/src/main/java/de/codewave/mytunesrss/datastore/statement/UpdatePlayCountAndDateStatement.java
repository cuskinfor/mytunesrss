package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistIdsQuery
 */
public class UpdatePlayCountAndDateStatement implements DataStoreStatement {
    private String[] myIds;

    public UpdatePlayCountAndDateStatement(String[] ids) {
        myIds = ids;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updatePlayCountAndDate");
        statement.setItems("id", myIds);
        statement.setLong("ts_played", System.currentTimeMillis());
        statement.execute();
    }
}