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

    public static FindTrackQuery getForAlbum(User user, String[] albums, String[] albumArtists, SortOrder sortOrder) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myAlbums = new String[albums.length];
        for (int i = 0; i < albums.length; i++) {
            query.myAlbums[i] = albums[i].toLowerCase();
        }
        query.myAlbumArtists = new String[albumArtists.length];
        for (int i = 0; i < albumArtists.length; i++) {
            query.myAlbumArtists[i] = albumArtists[i].toLowerCase();
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

    public static FindTrackQuery getMovies(User user) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.Movie;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(MediaType.Video);
        query.setVideoType(VideoType.Movie);
        return query;
    }

    public static FindTrackQuery getTvShowEpisodes(User user) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.TvShow;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(MediaType.Video);
        query.setVideoType(VideoType.TvShow);
        return query;
    }

    public static FindTrackQuery getTvShowSeriesEpisodes(User user, String series) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.TvShow;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(MediaType.Video);
        query.setVideoType(VideoType.TvShow);
        query.setSeries(series);
        return query;
    }

    public static FindTrackQuery getTvShowSeriesSeasonEpisodes(User user, String series, int season) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.TvShow;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(MediaType.Video);
        query.setVideoType(VideoType.TvShow);
        query.setSeries(series);
        query.setSeason(season);
        return query;
    }

    private List<String> myIds;
    private String[] myAlbums;
    private String[] myGenres;
    private String[] myArtists;
    private String[] myAlbumArtists;
    private SortOrder mySortOrder;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private ResultSetType myResultSetType = ResultSetType.TYPE_SCROLL_INSENSITIVE;
    private MediaType[] myMediaTypes;
    private VideoType myVideoType;
    private String mySeries;
    private Integer mySeason;

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

    public void setSeries(String series) {
        mySeries = series;
    }

    public void setSeason(Integer season) {
        mySeason = season;
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
        conditionals.put("moviesort", mySortOrder == SortOrder.Movie);
        conditionals.put("tvshowsort", mySortOrder == SortOrder.TvShow);
        conditionals.put("album", myAlbums != null && myAlbums.length > 0);
        conditionals.put("artist", myArtists != null && myArtists.length > 0);
        conditionals.put("albumartist", myAlbumArtists != null && myAlbumArtists.length > 0);
        conditionals.put("genre", myGenres != null && myGenres.length > 0);
        conditionals.put("mediatype", myMediaTypes != null && myMediaTypes.length > 0);
        conditionals.put("videotype", myVideoType != null);
        conditionals.put("tvshow", mySeries != null);
        conditionals.put("tvshowseason", mySeries != null && mySeason != null);
        conditionals.put("photosort", mySortOrder == SortOrder.Photos);
        statement = MyTunesRssUtils.createStatement(connection, "findTracks", conditionals);
        statement.setItems("album", myAlbums);
        statement.setItems("artist", myArtists);
        statement.setItems("albumartist", myAlbumArtists);
        statement.setItems("genre", myGenres);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        if (myMediaTypes != null && myMediaTypes.length > 0) {
            String[] mediaTypeNames = new String[myMediaTypes.length];
            for (int i = 0; i < myMediaTypes.length; i++) {
                mediaTypeNames[i] = myMediaTypes[i].name();
            }
            statement.setItems("mediaTypes", mediaTypeNames);
        }
        if (myVideoType != null) {
            statement.setString("videoType", myVideoType.name());
        }
        statement.setString("series", mySeries);
        if (mySeason != null) {
            statement.setInt("season", mySeason);
        }
        return execute(statement, new TrackResultBuilder());

    }
}