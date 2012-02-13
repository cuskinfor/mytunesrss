package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.RemoveOldTempPlaylistsStatement
 */
public class RemoveOldTempPlaylistsStatement implements DataStoreStatement {
    private static final Logger LOG = LoggerFactory.getLogger(RemoveOldTempPlaylistsStatement.class);
    private static final int DEFAULT_KEEP = 10000;
    private static final long ONE_YEAR = 365L * 24L * 3600L * 1000L;

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getTempPlaylistCreationTime");
        ResultSet rs = statement.executeQuery();
        if (rs.absolute(DEFAULT_KEEP)) {
            LOG.debug("Removing old temporary playlists, leaving about " + DEFAULT_KEEP + " playlists.");
            long ts = Math.max(rs.getLong("ts"), System.currentTimeMillis() - ONE_YEAR);
            statement = MyTunesRssUtils.createStatement(connection, "removeOldTempPlaylists");
            statement.setLong("ts", ts);
            statement.execute();
        }
    }
}
