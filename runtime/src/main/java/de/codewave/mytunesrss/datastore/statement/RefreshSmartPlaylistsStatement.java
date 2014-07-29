package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.lucene.LuceneTrackService;
import de.codewave.utils.sql.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Statement for updating all smart playlists.
 */
public class RefreshSmartPlaylistsStatement implements DataStoreStatement {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshSmartPlaylistsStatement.class);
    private static final long MILLIS_PER_DAY = 1000L * 3600L * 24L;
    private static final ConcurrentHashMap<String, AtomicInteger> LATCH_MAP = new ConcurrentHashMap<>();
    public enum UpdateType {
        SCHEDULED(), ON_PLAY(), DEFAULT()
    }
    private Collection<SmartInfo> mySmartInfos;
    private String myPlaylistId;
    private UpdateType myUpdateType = UpdateType.DEFAULT;

    public RefreshSmartPlaylistsStatement() {
        // nothing to do here
    }

    public RefreshSmartPlaylistsStatement(UpdateType updateType) {
        myUpdateType = updateType;
    }

    public RefreshSmartPlaylistsStatement(Collection<SmartInfo> smartInfos, String playlistId) {
        mySmartInfos = smartInfos;
        myPlaylistId = playlistId;
    }

    public void execute(Connection connection) throws SQLException {
        boolean anyPlaylistRefreshed = false;
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
                if (myUpdateType == UpdateType.DEFAULT || (myUpdateType == UpdateType.ON_PLAY && isOnPlayRefresh(smartInfos)) || (myUpdateType == UpdateType.SCHEDULED && isScheduledUpdateRefresh(smartInfos))) {
                    anyPlaylistRefreshed |= refreshSmartPlaylist(connection, smartInfos, smartPlaylist.getPlaylist().getId());
                    connection.commit();
                }
            }
        } else {
            anyPlaylistRefreshed |= refreshSmartPlaylist(connection, mySmartInfos, myPlaylistId);
        }
        if (anyPlaylistRefreshed) {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.MEDIA_SERVER_UPDATE));
            LOGGER.info("Smart playlists have been refreshed.");
        }
    }

    private boolean isOnPlayRefresh(Collection<SmartInfo> smartInfos) {
        for (SmartInfo smartInfo : smartInfos) {
            if (smartInfo.getFieldType() == SmartFieldType.order) {
                SmartOrder smartOrder = SmartOrder.valueOf(smartInfo.getPattern());
                return smartOrder == SmartOrder.lastplayed_asc || smartOrder == SmartOrder.lastplayed_desc || smartOrder == SmartOrder.playcount_asc || smartOrder == SmartOrder.playcount_desc;
            } else if (smartInfo.getFieldType() == SmartFieldType.recentlyPlayed) {
                return true;
            }
        }
        return false;
    }

    private boolean isScheduledUpdateRefresh(Collection<SmartInfo> smartInfos) {
        for (SmartInfo smartInfo : smartInfos) {
            if (smartInfo.getFieldType() == SmartFieldType.recentlyUpdated || smartInfo.getFieldType() == SmartFieldType.recentlyPlayed) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Float> toMap(Collection<LuceneTrackService.ScoredTrack> scoredTracks) {
        Map<String, Float> result = new HashMap<>();
        for (LuceneTrackService.ScoredTrack scoredTrack : scoredTracks) {
            result.put(scoredTrack.getId(), scoredTrack.getScore());
        }
        return result;
    }

    private boolean refreshSmartPlaylist(Connection connection, Collection<SmartInfo> smartInfos, String playlistId) throws SQLException {
        LATCH_MAP.putIfAbsent(playlistId, new AtomicInteger(0));
        if (LATCH_MAP.get(playlistId).getAndIncrement() < 2) { // at most two concurrent requests may enter here at any time
            synchronized (LATCH_MAP.get(playlistId)) { // only one request may enter at any time and only one (see above) is allowed to wait
                LOGGER.info("Refreshing smart playlist with id \"" + playlistId + "\".");
                try {
                    if (SmartInfo.isLuceneCriteria(smartInfos)) {
                        Collection<LuceneTrackService.ScoredTrack> scoredTracks = MyTunesRss.LUCENE_TRACK_SERVICE.searchTracks(smartInfos, 0, 10000);
                        MyTunesRssUtils.createStatement(connection, "createSearchTempTables").execute(); // create if not exists
                        MyTunesRssUtils.createStatement(connection, "truncateSearchTempTables").execute(); // truncate if already existed
                        if (!CollectionUtils.isEmpty(scoredTracks)) {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "fillLuceneSearchTempTable");
                            statement.setObject("track", toMap(scoredTracks));
                            statement.execute();
                        }
                    }
                    Map<String, Boolean> conditionals = new HashMap<>();
                    conditionals.put("order_default", true);
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
                            case recentlyPlayed:
                                conditionals.put((smartInfo.isInvert() ? "not_" : "") + "recently_played", true);
                                break;
                            case recentlyUpdated:
                                conditionals.put((smartInfo.isInvert() ? "not_" : "") + "recently_updated", true);
                                break;
                            case order:
                                conditionals.put("order_default", false);
                                switch (SmartOrder.valueOf(smartInfo.getPattern())) {
                                    case random:
                                        conditionals.put("order_random", true);
                                        break;
                                    case lastplayed_asc:
                                        conditionals.put("order_lastplayed", true);
                                        break;
                                    case lastplayed_desc:
                                        conditionals.put("order_lastplayed", true);
                                        conditionals.put("order_desc", true);
                                        break;
                                    case lastupdate_asc:
                                        conditionals.put("order_lastupdate", true);
                                        break;
                                    case lastupdate_desc:
                                        conditionals.put("order_lastupdate", true);
                                        conditionals.put("order_desc", true);
                                        break;
                                    case playcount_asc:
                                        conditionals.put("order_playcount", true);
                                        break;
                                    case playcount_desc:
                                        conditionals.put("order_playcount", true);
                                        conditionals.put("order_desc", true);
                                        break;
                                    default:
                                        // no conditionals in other cases
                                }
                                break;
                            case sizeLimit:
                                conditionals.put("limit", true);
                                break;
                            default:
                                // no conditionals in other cases
                        }
                    }
                    final SmartStatement queryStatement = MyTunesRssUtils.createStatement(connection, "getTracksForSmartPlaylist", conditionals);
                    for (SmartInfo smartInfo : smartInfos) {
                        switch (smartInfo.getFieldType()) {
                            case mintime:
                                queryStatement.setInt("time_min", Integer.parseInt(smartInfo.getPattern()));
                                break;
                            case datasource:
                                queryStatement.setString("source_id", smartInfo.getPattern());
                                break;
                            case maxtime:
                                queryStatement.setInt("time_max", Integer.parseInt(smartInfo.getPattern()));
                                break;
                            case mediatype:
                                queryStatement.setString("mediatype", smartInfo.getPattern());
                                break;
                            case protection:
                                queryStatement.setBoolean("protected", Boolean.parseBoolean(smartInfo.getPattern()));
                                break;
                            case videotype:
                                queryStatement.setString("videotype", smartInfo.getPattern());
                                break;
                            case sizeLimit:
                                queryStatement.setInt("maxCount", Integer.parseInt(smartInfo.getPattern()));
                                break;
                            case recentlyPlayed:
                                queryStatement.setLong("ts_played", System.currentTimeMillis() - (MILLIS_PER_DAY * Long.parseLong(smartInfo.getPattern())));
                                break;
                            case recentlyUpdated:
                                queryStatement.setLong("ts_updated", System.currentTimeMillis() - (MILLIS_PER_DAY * Long.parseLong(smartInfo.getPattern())));
                                break;
                            default:
                                // nothing in all other cases
                        }
                    }
                    Collection<String> tracks = new LinkedHashSet<>();
                    DataStoreQuery<QueryResult<String>> dataStoreQuery = new DataStoreQuery<QueryResult<String>>() {
                        @Override
                        public QueryResult<String> execute(Connection connection) throws SQLException {
                            return execute(queryStatement, new ResultBuilder<String>() {
                                public String create(ResultSet resultSet) throws SQLException {
                                    return resultSet.getString(1);
                                }
                            });
                        }
                    };
                    dataStoreQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 10000);
                    dataStoreQuery.execute(connection).addRemainingResults(tracks);
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "updateSmartPlaylist");
                    statement.setString("id", playlistId);
                    statement.setObject("track_id", tracks);
                    synchronized (LATCH_MAP) { // synchronize globally to prevent database locks
                        statement.execute();
                    }
                    return true;
                } catch (IOException | ParseException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could update smart playlist.", e);
                    }
                } finally {
                    LATCH_MAP.get(playlistId).decrementAndGet();
                }
            }
        }
        return false;
    }
}
