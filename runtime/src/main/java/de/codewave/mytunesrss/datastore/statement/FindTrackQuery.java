/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.lucene.LuceneQueryParserException;
import de.codewave.mytunesrss.lucene.LuceneTrackService;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQuery
 */
public class FindTrackQuery extends MyTunesRssDataStoreQuery<QueryResult<Track>> {

    public static FindTrackQuery getForIds(String[] trackIds) {
        FindTrackQuery query = new FindTrackQuery();
        query.myScoredTracks = new HashMap<>();
        int i = trackIds.length;
        for (String trackId : trackIds) {
            query.myScoredTracks.put(trackId, (float) i--);
        }
        return query;
    }

    private static Map<String, Float> toMap(Collection<LuceneTrackService.ScoredTrack> scoredTracks) {
        Map<String, Float> result = new HashMap<>();
        for (LuceneTrackService.ScoredTrack scoredTrack : scoredTracks) {
            result.put(scoredTrack.getId(), scoredTrack.getScore());
        }
        return result;
    }

    public static FindTrackQuery getForSearchTerm(User user, String searchTerm, int fuzziness, SortOrder sortOrder, int maxResults) throws IOException {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        String[] searchTerms = StringUtils.split(StringUtils.defaultString(StringUtils.lowerCase(searchTerm)), " ");
        Collection<LuceneTrackService.ScoredTrack> scoredTracks = MyTunesRss.LUCENE_TRACK_SERVICE.searchTracks(searchTerms, fuzziness, maxResults);
        query.myScoredTracks = scoredTracks.isEmpty() ? Collections.singletonMap("ThisDummyIdWillNeverExist", 0f) : toMap(scoredTracks);
        query.myMediaTypes = getQueryMediaTypes(user);
        query.myPermittedDataSources = getPermittedDataSources(user);
        return query;
    }

    public static FindTrackQuery getForExpertSearchTerm(User user, String searchTerm, SortOrder sortOrder, int maxResults) throws IOException, LuceneQueryParserException {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = sortOrder;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        Collection<LuceneTrackService.ScoredTrack> scoredTracks = MyTunesRss.LUCENE_TRACK_SERVICE.searchTracks(searchTerm, maxResults);
        query.myScoredTracks = scoredTracks.isEmpty() ? Collections.singletonMap("ThisDummyIdWillNeverExist", 0f) : toMap(scoredTracks);
        query.myMediaTypes = getQueryMediaTypes(user);
        query.myPermittedDataSources = getPermittedDataSources(user);
        return query;
    }

    static MediaType[] getQueryMediaTypes(User user, MediaType... mediaTypes) {
        if (!user.isAudio() || !user.isVideo()) {
            Set<MediaType> resultTypes = mediaTypes != null && mediaTypes.length > 0 ? new HashSet<>(Arrays.asList(mediaTypes)) : new HashSet<>(Arrays.asList(MediaType.values()));
            if (!user.isAudio()) {
                resultTypes.remove(MediaType.Audio);
            }
            if (!user.isVideo()) {
                resultTypes.remove(MediaType.Video);
            }
            return resultTypes.toArray(new MediaType[resultTypes.size()]);
        } else {
            return mediaTypes;
        }
    }

    static String[] getPermittedDataSources(User user) {
        Set<String> ids = user.getPermittedDataSourceIds();
        return ids != null ? ids.toArray(new String[ids.size()]) : null;
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
        query.setMediaTypes(getQueryMediaTypes(user, MediaType.Audio));
        query.myPermittedDataSources = getPermittedDataSources(user);
        query.setForceEmptyResult(!user.isAudio());
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
        query.setMediaTypes(getQueryMediaTypes(user, MediaType.Audio));
        query.myPermittedDataSources = getPermittedDataSources(user);
        query.setForceEmptyResult(!user.isAudio());
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
        query.setMediaTypes(getQueryMediaTypes(user, MediaType.Audio));
        query.myPermittedDataSources = getPermittedDataSources(user);
        query.setForceEmptyResult(!user.isAudio());
        return query;
    }

    public static FindTrackQuery getMovies(User user) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.Movie;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(getQueryMediaTypes(user, MediaType.Video));
        query.setVideoType(VideoType.Movie);
        query.myPermittedDataSources = getPermittedDataSources(user);
        query.setForceEmptyResult(!user.isVideo());
        return query;
    }

    public static FindTrackQuery getTvShowEpisodes(User user) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.TvShow;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(getQueryMediaTypes(user, MediaType.Video));
        query.setVideoType(VideoType.TvShow);
        query.myPermittedDataSources = getPermittedDataSources(user);
        query.setForceEmptyResult(!user.isVideo());
        return query;
    }

    public static FindTrackQuery getTvShowSeriesEpisodes(User user, String series) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.TvShow;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(getQueryMediaTypes(user, MediaType.Video));
        query.setVideoType(VideoType.TvShow);
        query.setSeries(series);
        query.myPermittedDataSources = getPermittedDataSources(user);
        query.setForceEmptyResult(!user.isVideo());
        return query;
    }

    public static FindTrackQuery getTvShowSeriesSeasonEpisodes(User user, String series, int season) {
        FindTrackQuery query = new FindTrackQuery();
        query.mySortOrder = SortOrder.TvShow;
        query.myRestrictedPlaylistIds = user.getRestrictedPlaylistIds();
        query.myExcludedPlaylistIds = user.getExcludedPlaylistIds();
        query.setMediaTypes(getQueryMediaTypes(user, MediaType.Video));
        query.setVideoType(VideoType.TvShow);
        query.setSeries(series);
        query.setSeason(season);
        query.myPermittedDataSources = getPermittedDataSources(user);
        query.setForceEmptyResult(!user.isVideo());
        return query;
    }

    private Map<String, Float> myScoredTracks;
    private String[] myAlbums;
    private String[] myGenres;
    private String[] myArtists;
    private String[] myAlbumArtists;
    private SortOrder mySortOrder;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();
    private List<String> myExcludedPlaylistIds = Collections.emptyList();
    private MediaType[] myMediaTypes;
    private VideoType myVideoType;
    private String mySeries;
    private Integer mySeason;
    private String[] myPermittedDataSources;

    private FindTrackQuery() {
        // intentionally left blank
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

    @Override
    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        Map<String, Boolean> conditionals = new HashMap<>();
        MyTunesRssUtils.createStatement(connection, "createSearchTempTables").execute(); // create if not exists
        MyTunesRssUtils.createStatement(connection, "truncateSearchTempTables").execute(); // truncate if already existed
        if (!CollectionUtils.isEmpty(myScoredTracks)) {
            statement = MyTunesRssUtils.createStatement(connection, "fillLuceneSearchTempTable");
            statement.setObject("track", myScoredTracks);
            statement.execute();
            conditionals.put("temptables", Boolean.TRUE);
        }
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("excluded", !myExcludedPlaylistIds.isEmpty());
        conditionals.put("album", myAlbums != null && myAlbums.length > 0);
        conditionals.put("artist", myArtists != null && myArtists.length > 0);
        conditionals.put("albumartist", myAlbumArtists != null && myAlbumArtists.length > 0);
        conditionals.put("genre", myGenres != null && myGenres.length > 0);
        conditionals.put("mediatype", myMediaTypes != null && myMediaTypes.length > 0);
        conditionals.put("videotype", myVideoType != null);
        conditionals.put("tvshow", mySeries != null);
        conditionals.put("tvshowseason", mySeries != null && mySeason != null);
        conditionals.put("photosort", mySortOrder == SortOrder.Photos);
        conditionals.put("luceneAdditionalSort", mySortOrder == SortOrder.Artist || mySortOrder == SortOrder.Album || mySortOrder == SortOrder.Movie || mySortOrder == SortOrder.TvShow);
        conditionals.put("artistsort", mySortOrder == SortOrder.Artist);
        conditionals.put("albumsort", mySortOrder == SortOrder.Album);
        conditionals.put("moviesort", mySortOrder == SortOrder.Movie);
        conditionals.put("tvshowsort", mySortOrder == SortOrder.TvShow);
        conditionals.put("sorting", mySortOrder == SortOrder.Artist || mySortOrder == SortOrder.Album || mySortOrder == SortOrder.Movie || mySortOrder == SortOrder.TvShow || !CollectionUtils.isEmpty(myScoredTracks));
        conditionals.put("datasource", myPermittedDataSources != null);
        statement = MyTunesRssUtils.createStatement(connection, "findTracks", conditionals);
        statement.setItems("album", myAlbums);
        statement.setItems("artist", myArtists);
        statement.setItems("albumartist", myAlbumArtists);
        statement.setItems("genre", myGenres);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        statement.setItems("excludedPlaylistIds", myExcludedPlaylistIds);
        statement.setItems("datasources", myPermittedDataSources);
        setQueryMediaTypes(statement, myMediaTypes);
        if (myVideoType != null) {
            statement.setString("videoType", myVideoType.name());
        }
        statement.setString("series", mySeries);
        if (mySeason != null) {
            statement.setInt("season", mySeason);
        }
        return execute(statement, new TrackResultBuilder());

    }

    static void setQueryMediaTypes(SmartStatement statement, MediaType[] mediaTypes) throws SQLException {
        if (mediaTypes != null && mediaTypes.length > 0) {
            String[] mediaTypeNames = new String[mediaTypes.length];
            for (int i = 0; i < mediaTypes.length; i++) {
                mediaTypeNames[i] = mediaTypes[i].name();
            }
            statement.setItems("mediaTypes", mediaTypeNames);
        }
    }
}
