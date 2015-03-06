package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindPlaylistIdsQuery
 */
public class UpdatePlayCountAndDateStatement implements DataStoreStatement {
    private String[] myIds;

    public UpdatePlayCountAndDateStatement(String[] ids) {
        myIds = ids != null ? ids.clone() : null;
    }

    @Override
    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updatePlayCountAndDate");
        statement.setItems("id", myIds);
        statement.setLong("ts_played", System.currentTimeMillis());
        statement.execute();
    }
}
