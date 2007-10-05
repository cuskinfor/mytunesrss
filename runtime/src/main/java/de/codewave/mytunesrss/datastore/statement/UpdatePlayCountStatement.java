package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.*;

import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistIdsQuery
 */
public class UpdatePlayCountStatement implements DataStoreStatement {
    private String[] myIds;

    public UpdatePlayCountStatement(String[] ids) {
        myIds = ids;
    }

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updatePlayCount");
        statement.setItems("id", myIds);
        statement.execute();
    }
}