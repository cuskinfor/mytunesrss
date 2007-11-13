package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import java.sql.*;

/**
 * de.codewave.mytunesrss.datastore.statement.LastImageUpdateTimeStatement
 */
public class LastImageUpdateTimeStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(InsertOrUpdateImageStatement.class);

    private String myTrackId;

    public LastImageUpdateTimeStatement(String trackId) {
        myTrackId = trackId;
    }

    public synchronized void execute(Connection connection) throws SQLException {
        try {
            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updateLastImageUpdateTime");
            statement.setString("track_id", myTrackId);
            statement.setLong("currentTime", System.currentTimeMillis());
            statement.execute();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("Could not update last image update time.", myTrackId), e);
            }
        }
    }
}