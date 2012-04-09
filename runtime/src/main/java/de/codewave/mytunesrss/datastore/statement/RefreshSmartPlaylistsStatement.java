package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
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
    private SmartInfo mySmartInfo;
    private String myPlaylistId;

    public RefreshSmartPlaylistsStatement() {
        // nothing to do here
    }

    public RefreshSmartPlaylistsStatement(SmartInfo smartInfo, String playlistId) {
        mySmartInfo = smartInfo;
        myPlaylistId = playlistId;
    }

    public void execute(Connection connection) throws SQLException {
        MyTunesRssUtils.createStatement(connection, "createSearchTempTables").execute(); // create if not exists
        if (mySmartInfo == null || StringUtils.isBlank(myPlaylistId)) {
            List<SmartPlaylist> smartPlaylists = new DataStoreQuery<List<SmartPlaylist>>() {
                @Override
                public List<SmartPlaylist> execute(Connection connection) throws SQLException {
                    return execute(MyTunesRssUtils.createStatement(connection, "findAllSmartPlaylists"), new SmartPlaylistResultBuilder()).getResults();
                }
            }.execute(connection);
            for (SmartPlaylist smartPlaylist : smartPlaylists) {
                SmartInfo smartInfo = smartPlaylist.getSmartInfo();
                refreshSmartPlaylist(connection, smartInfo, smartPlaylist.getPlaylist().getId());
            }
        } else {
            refreshSmartPlaylist(connection, mySmartInfo, myPlaylistId);
        }
        LOGGER.info("Smart playlists have been refreshed.");
    }

    private void refreshSmartPlaylist(Connection connection, SmartInfo smartInfo, String playlistId) throws SQLException {
        try {
            if (smartInfo.isLuceneCriteria()) {
                Collection<String> trackIds = MyTunesRss.LUCENE_TRACK_SERVICE.searchTrackIds(smartInfo, 0);
                MyTunesRssUtils.createStatement(connection, "truncateSearchTempTables").execute(); // truncate if already existed
                if (!CollectionUtils.isEmpty(trackIds)) {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "fillLuceneSearchTempTable");
                    statement.setObject("track_id", trackIds);
                    statement.execute();
                }
            }
            Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
            conditionals.put("lucene", smartInfo.isLuceneCriteria());
            conditionals.put("nolucene", !smartInfo.isLuceneCriteria());
            conditionals.put("mintime", smartInfo.getTimeMin() != null);
            conditionals.put("maxtime", smartInfo.getTimeMax() != null);
            conditionals.put("mediatype", smartInfo.getMediaType() != null);
            conditionals.put("videotype", smartInfo.getVideoType() != null);
            conditionals.put("protected", smartInfo.getProtected() != null);
            conditionals.put("sourceid", StringUtils.isNotBlank(smartInfo.getSourceId()));
            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "refreshSmartPlaylist", conditionals);
            statement.setString("id", playlistId);
            statement.setString("source_id", smartInfo.getSourceId());
            if (smartInfo.getTimeMin() != null) {
                statement.setInt("time_min", smartInfo.getTimeMin());
            }
            if (smartInfo.getTimeMax() != null) {
                statement.setInt("time_max", smartInfo.getTimeMax());
            }
            if (smartInfo.getMediaType() != null) {
                statement.setString("mediatype", smartInfo.getMediaType().name());
            }
            if (smartInfo.getVideoType() != null) {
                statement.setString("videotype", smartInfo.getVideoType().name());
            }
            if (smartInfo.getProtected() != null) {
                statement.setBoolean("protected", smartInfo.getProtected());
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
    }
}
