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
    private Collection<SmartInfo> mySmartInfos;
    private String myPlaylistId;

    public RefreshSmartPlaylistsStatement() {
        // nothing to do here
    }

    public RefreshSmartPlaylistsStatement(Collection<SmartInfo> smartInfos, String playlistId) {
        mySmartInfos = smartInfos;
        myPlaylistId = playlistId;
    }

    public void execute(Connection connection) throws SQLException {
        if (mySmartInfos == null || mySmartInfos.isEmpty() || StringUtils.isBlank(myPlaylistId)) {
            Collection<SmartPlaylist> smartPlaylists = new DataStoreQuery<Collection<SmartPlaylist>>() {
                @Override
                public Collection<SmartPlaylist> execute(Connection connection) throws SQLException {
                    SmartPlaylistResultBuilder builder = new SmartPlaylistResultBuilder();
                    execute(MyTunesRssUtils.createStatement(connection, "findAllSmartPlaylists"), builder).getResults();
                    return builder.getSmartPlaylists();
                }
            }.execute(connection);
            for (SmartPlaylist smartPlaylist : smartPlaylists) {
                Collection<SmartInfo> smartInfos = smartPlaylist.getSmartInfos();
                refreshSmartPlaylist(connection, smartInfos, smartPlaylist.getPlaylist().getId());
                connection.commit();
            }
        } else {
            refreshSmartPlaylist(connection, mySmartInfos, myPlaylistId);
        }
        LOGGER.info("Smart playlists have been refreshed.");
    }

    private void refreshSmartPlaylist(Connection connection, Collection<SmartInfo> smartInfos, String playlistId) throws SQLException {
        LOGGER.debug("Refreshing smart playlist with id \"" + playlistId + "\".");
        try {
            if (SmartInfo.isLuceneCriteria(smartInfos)) {
                Collection<String> trackIds = MyTunesRss.LUCENE_TRACK_SERVICE.searchTrackIds(smartInfos, 0, 10000);
                MyTunesRssUtils.createStatement(connection, "createSearchTempTables").execute(); // create if not exists
                MyTunesRssUtils.createStatement(connection, "truncateSearchTempTables").execute(); // truncate if already existed
                if (!CollectionUtils.isEmpty(trackIds)) {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "fillLuceneSearchTempTable");
                    statement.setObject("track_id", trackIds);
                    statement.execute();
                }
            }
            Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
            conditionals.put("order", true);
            conditionals.put("lucene", SmartInfo.isLuceneCriteria(smartInfos));
            conditionals.put("nolucene", !SmartInfo.isLuceneCriteria(smartInfos));
            for (SmartInfo smartInfo : smartInfos) {
                switch (smartInfo.getFieldType()) {
                    case mintime:
                        conditionals.put("mintime", true);
                        break;
                    case datasource:
                        conditionals.put("datasource", true);
                        break;
                    case maxtime:
                        conditionals.put("maxtime", true);
                        break;
                    case mediatype:
                        conditionals.put("mediatype", true);
                        break;
                    case protection:
                        conditionals.put("protected", true);
                        break;
                    case videotype:
                        conditionals.put("videotype", true);
                        break;
                    case randomOrder:
                        conditionals.remove("order");
                        conditionals.put("random", true);
                        break;
                    case sizeLimit:
                        conditionals.put("limit", true);
                        break;
                }
            }
            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "refreshSmartPlaylist", conditionals);
            statement.setString("id", playlistId);
            for (SmartInfo smartInfo : smartInfos) {
                switch (smartInfo.getFieldType()) {
                    case mintime:
                        statement.setInt("time_min", Integer.parseInt(smartInfo.getPattern()));
                        break;
                    case datasource:
                        statement.setString("source_id", smartInfo.getPattern());
                        break;
                    case maxtime:
                        statement.setInt("time_max", Integer.parseInt(smartInfo.getPattern()));
                        break;
                    case mediatype:
                        statement.setString("mediatype", smartInfo.getPattern());
                        break;
                    case protection:
                        statement.setBoolean("protected", Boolean.parseBoolean(smartInfo.getPattern()));
                        break;
                    case videotype:
                        statement.setString("videotype", smartInfo.getPattern());
                        break;
                    case sizeLimit:
                        statement.setInt("maxCount", Integer.parseInt(smartInfo.getPattern()));
                        break;
                }
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
