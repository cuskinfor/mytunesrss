/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

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
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        String[] searchTerms = StringUtils.split(StringUtils.defaultString(StringUtils.lowerCase(searchTerm)), " ");
        Collection<String> luceneResult = MyTunesRss.LUCENE_TRACK_SERVICE.searchTrackIds(searchTerms, fuzziness);
        query.myIds = luceneResult.isEmpty() ? Collections.singletonList("ThisDummyIdWillNeverExist") : new ArrayList<String>(luceneResult);
        return query;
    }

    public static FindTrackQuery getForExpertSearchTerm(User user, String searchTerm, SortOrder sortOrder) throws IOException, ParseException, LuceneQueryParserException {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        Collection<String> luceneResult = MyTunesRss.LUCENE_TRACK_SERVICE.searchTrackIds(searchTerm);
        query.myIds = luceneResult.isEmpty() ? Collections.singletonList("ThisDummyIdWillNeverExist") : new ArrayList<String>(luceneResult);
        return query;
    }

    public static FindTrackQuery getForAlbum(User user, String[] albums, SortOrder sortOrder) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myAlbums = new String[albums.length];
        for (int i = 0; i < albums.length; i++) {
            query.myAlbums[i] = albums[i].toLowerCase();
        }
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(MediaType.Audio);
        return query;
    }

    public static FindTrackQuery getForArtist(User user, String[] artists, SortOrder sortOrder) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myArtists = new String[artists.length];
        for (int i = 0; i < artists.length; i++) {
            query.myArtists[i] = artists[i].toLowerCase();
        }
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(MediaType.Audio);
        return query;
    }

    public static FindTrackQuery getForGenre(User user, String[] genres, SortOrder sortOrder) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myGenres = new String[genres.length];
        for (int i = 0; i < genres.length; i++) {
            query.myGenres[i] = genres[i].toLowerCase();
        }
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(MediaType.Audio);
        return query;
    }

    public static FindTrackQuery getForMediaAndVideoTypes(User user, MediaType[] mediaTypes, VideoType videoType) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.Abc;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(mediaTypes);
        query.setVideoType(videoType);
        return query;
    }

    private List<String> myIds;
    private String[] myAlbums;
    private String[] myGenres;
    private String[] myArtists;
    private SortOrder mySortOrder;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private ResultSetType myResultSetType = ResultSetType.TYPE_SCROLL_INSENSITIVE;
    private MediaType[] myMediaTypes;
    private VideoType myVideoType;

    private FindTrackQuery() {
        // intentionally left blank
    }

    public void setResultSetType(ResultSetType resultSetType) {
        myResultSetType = resultSetType;
    }

    public void setMediaTypes(MediaType... mediaTypes) {
        myMediaTypes = mediaTypes;
    }

    public void setVideoType(VideoType videoType) {
        myVideoType = videoType;
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        MyTunesRssUtils.createStatement(connection, "createSearchTempTables").execute(); // create if not exists
        MyTunesRssUtils.createStatement(connection, "truncateSearchTempTables").execute(); // truncate if already existed
        if (!CollectionUtils.isEmpty(myIds)) {
            statement = MyTunesRssUtils.createStatement(connection, "fillLuceneSearchTempTable", myResultSetType);
            statement.setObject("track_id", myIds);
            statement.execute();
            conditionals.put("temptables", Boolean.TRUE);
        }
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("artistsort", mySortOrder == SortOrder.Artist);
        conditionals.put("albumsort", mySortOrder == SortOrder.Album);
        conditionals.put("abcsort", mySortOrder == SortOrder.Abc);
        conditionals.put("album", myAlbums != null && myAlbums.length > 0);
        conditionals.put("artist", myArtists != null && myArtists.length > 0);
        conditionals.put("genre", myGenres != null && myGenres.length > 0);
        conditionals.put("mediatype", myMediaTypes != null && myMediaTypes.length > 0);
        conditionals.put("videotype", myVideoType != null);
        statement = MyTunesRssUtils.createStatement(connection, "findTracks", conditionals);
        statement.setItems("album", myAlbums);
        statement.setItems("artist", myArtists);
        statement.setItems("genre", myGenres);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        if (myMediaTypes != null && myMediaTypes.length > 0) {
            statement.setItems("mediaTypes", myMediaTypes);
        }
        if (myVideoType != null) {
            statement.setString("videoType", myVideoType.name());
        }
        return execute(statement, new TrackResultBuilder());

    }
}