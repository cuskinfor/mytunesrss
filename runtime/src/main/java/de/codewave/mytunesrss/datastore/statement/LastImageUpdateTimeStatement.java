package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;

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
            statement.setLong("updateTime", System.currentTimeMillis());
            statement.execute();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(String.format("Could not update last image update time.", myTrackId), e);
            }
        }
    }
}