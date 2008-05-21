package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.RemoveOldTempPlaylistsStatement
 */
public class RemoveOldTempPlaylistsStatement implements DataStoreStatement {
    private static final Log LOG = LogFactory.getLog(RemoveOldTempPlaylistsStatement.class);
    private static final int DEFAULT_KEEP = 10000;

    public void execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getTempPlaylistCreationTime");
        ResultSet rs = statement.executeQuery();
        if (rs.absolute(DEFAULT_KEEP)) {
            LOG.debug("Removing old temporary playlists, leaving about " + DEFAULT_KEEP + " playlists.");
            long ts = rs.getLong("ts");
            statement = MyTunesRssUtils.createStatement(connection, "removeOldTempPlaylists");
            statement.setLong("ts", ts);
            statement.execute();
        }
    }
}
