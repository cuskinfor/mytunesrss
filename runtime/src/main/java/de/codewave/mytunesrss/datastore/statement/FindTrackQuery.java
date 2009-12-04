/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQuery
 */
public class FindTrackQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Track>> {

    public static FindTrackQuery getForIds(String[] trackIds) {
        FindTrackQuery query = new FindTrackQuery();
        query.myIds = Arrays.asList(trackIds);
        return query;
    }

    public static FindTrackQuery getForSearchTerm(User user, String searchTerm, int fuzziness, SortOrder sortOrder) throws IOException, ParseException {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myRestrictedPlaylistIds = user.getPlaylistIds();
        String[] searchTerms = StringUtils.split(StringUtils.defaultString(StringUtils.lowerCase(searchTerm)), " ");
        List<String> luceneResult = MyTunesRss.LUCENE_TRACK_SERVICE.searchTrackIds(searchTerms, fuzziness);
        query.myIds = luceneResult.isEmpty() ? Collections.singletonList("ThisDummyIdWillNeverExist") : luceneResult;
        return query;
    }

    public static FindTrackQuery getForAlbum(User user, String[] albums, SortOrder sortOrder) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myAlbums = new String[albums.length];
        for (int i = 0; i < albums.length; i++) {
            query.myAlbums[i] = albums[i].toLowerCase();
        }
        query.myRestrictedPlaylistIds = user.getPlaylistIds();
        return query;
    }

    public static FindTrackQuery getForArtist(User user, String[] artists, SortOrder sortOrder) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myArtists = new String[artists.length];
        for (int i = 0; i < artists.length; i++) {
            query.myArtists[i] = artists[i].toLowerCase();
        }
        query.myRestrictedPlaylistIds = user.getPlaylistIds();
        return query;
    }

    public static FindTrackQuery getForGenre(User user, String[] genres, SortOrder sortOrder) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myGenres = new String[genres.length];
        for (int i = 0; i < genres.length; i++) {
            query.myGenres[i] = genres[i].toLowerCase();
        }
        query.myRestrictedPlaylistIds = user.getPlaylistIds();
        return query;
    }

    private List<String> myIds;
    private String[] myAlbums;
    private String[] myGenres;
    private String[] myArtists;
    private SortOrder mySortOrder;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();

    private FindTrackQuery() {
        // intentionally left blank
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        MyTunesRssUtils.createStatement(connection, "createSearchTempTables").execute(); // create if not exists
        MyTunesRssUtils.createStatement(connection, "truncateSearchTempTables").execute(); // truncate if already existed
        if (!CollectionUtils.isEmpty(myIds)) {
            statement = MyTunesRssUtils.createStatement(connection, "fillLuceneSearchTempTable");
            statement.setObject("track_id", myIds);
            statement.execute();
            conditionals.put("temptables", Boolean.TRUE);
        }
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("artistsort", mySortOrder == SortOrder.Artist);
        conditionals.put("albumsort", mySortOrder == SortOrder.Album);
        conditionals.put("album", myAlbums != null && myAlbums.length > 0);
        conditionals.put("artist", myArtists != null && myArtists.length > 0);
        conditionals.put("genre", myGenres != null && myGenres.length > 0);
        statement = MyTunesRssUtils.createStatement(connection, "findTracks", conditionals);
        statement.setItems("album", myAlbums);
        statement.setItems("artist", myArtists);
        statement.setItems("genre", myGenres);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        return execute(statement, new TrackResultBuilder());

    }
}