package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventListener;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Statement for updating all smart playlists. Also an event listener, so the update is run in case of an event of type SMART_INFO_CHANGED or
 * DATABASE_UPFDATE_FINISHED.
 */
public class RefreshSmartPlaylistsStatement implements DataStoreStatement, MyTunesRssEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshSmartPlaylistsStatement.class);

    public void execute(Connection connection) throws SQLException {
        MyTunesRssUtils.createStatement(connection, "refreshSmartPlaylists").execute();
    }

    public void handleEvent(MyTunesRssEvent event) {
        if (event == MyTunesRssEvent.SMART_INFO_CHANGED || event == MyTunesRssEvent.DATABASE_UPDATE_FINISHED) {
            LOGGER.debug("Got event \"" + event.name() + "\" => Updating smart playlists.");
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            try {
                session.executeStatement(this);
                session.commit();
                LOGGER.debug("Finished updating smart playlists.");
            } catch (SQLException e) {
                LOGGER.error("Could not update smart playlists.", e);
                try {
                    session.rollback();
                } catch (SQLException e1) {
                    LOGGER.error("Could not rollback transaction.", e1);
                }
            }
        }
    }
}
