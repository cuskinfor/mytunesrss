package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.lucene.queryParser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Statement for updating all smart playlists.
 */
public class RefreshSmartPlaylistsStatement implements DataStoreStatement {
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshSmartPlaylistsStatement.class);

    public void execute(Connection connection) throws SQLException {
        List<SmartPlaylist> smartPlaylists = new DataStoreQuery<List<SmartPlaylist>>() {
            @Override
            public List<SmartPlaylist> execute(Connection connection) throws SQLException {
                return execute(MyTunesRssUtils.createStatement(connection, "findAllSmartPlaylists"), new SmartPlaylistResultBuilder()).getResults();
            }
        }.execute(connection);
        MyTunesRssUtils.createStatement(connection, "createSearchTempTables").execute(); // create if not exists
        for (SmartPlaylist smartPlaylist : smartPlaylists) {
            try {
                Collection<String> trackIds = MyTunesRss.LUCENE_TRACK_SERVICE.searchTrackIds(smartPlaylist.getSmartInfo(), 0);
                MyTunesRssUtils.createStatement(connection, "truncateSearchTempTables").execute(); // truncate if already existed
                if (!CollectionUtils.isEmpty(trackIds)) {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "fillLuceneSearchTempTable");
                    statement.setObject("track_id", trackIds);
                    statement.execute();
                }
                Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
                conditionals.put("mintime", smartPlaylist.getSmartInfo().getTimeMin() != null);
                conditionals.put("maxtime", smartPlaylist.getSmartInfo().getTimeMax() != null);
                conditionals.put("mediatype", smartPlaylist.getSmartInfo().getMediaType() != null);
                conditionals.put("protected", smartPlaylist.getSmartInfo().getProtected() != null);
                SmartStatement statement = MyTunesRssUtils.createStatement(connection, "refreshSmartPlaylist", conditionals);
                statement.setString("id", smartPlaylist.getPlaylist().getId());
                if (smartPlaylist.getSmartInfo().getTimeMin() != null) {
                    statement.setInt("time_min", smartPlaylist.getSmartInfo().getTimeMin());
                }
                if (smartPlaylist.getSmartInfo().getTimeMax() != null) {
                    statement.setInt("time_max", smartPlaylist.getSmartInfo().getTimeMax());
                }
                if (smartPlaylist.getSmartInfo().getMediaType() != null) {
                    statement.setString("mediatype", smartPlaylist.getSmartInfo().getMediaType().name());
                }
                if (smartPlaylist.getSmartInfo().getProtected() != null) {
                    statement.setBoolean("protected", smartPlaylist.getSmartInfo().getProtected());
                }
                statement.execute();
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could update smart playlist.", e);
                }
            } catch (ParseException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could update smart playlist.", e);
                }
            }
            LOGGER.info("Smart playlists have been refreshed.");
        }
    }
}
