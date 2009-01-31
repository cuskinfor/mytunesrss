package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreStatement;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Statement for updating all smart playlists.
 */
public class RefreshSmartPlaylistsStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshSmartPlaylistsStatement.class);

    public void execute(Connection connection) throws SQLException {
        MyTunesRssUtils.createStatement(connection, "refreshSmartPlaylists").execute();
        LOGGER.info("Smart playlists have been refreshed.");
    }
}
