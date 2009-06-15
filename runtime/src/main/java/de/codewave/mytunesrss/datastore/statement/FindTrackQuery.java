/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindTrackQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Track>> {

    public static FindTrackQuery getForIds(String[] trackIds) {
        FindTrackQuery query = new FindTrackQuery();
        query.myIds = Arrays.asList(trackIds);
        return query;
    }


    public static FindTrackQuery getForSearchTerm(User user, String searchTerm, boolean fuzzy, boolean sortByArtistFirst) throws IOException, ParseException {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        query.myRestrictedPlaylistId = user.getPlaylistId();
        String[] searchTerms = StringUtils.split(StringUtils.defaultString(searchTerm), " ");
        query.myIds = MyTunesRss.LUCENE_TRACK_SERVICE.searchTrackIds(searchTerms, fuzzy);
        return CollectionUtils.isEmpty(query.myIds) ? null : query;
    }

    public static FindTrackQuery getForAlbum(User user, String[] albums, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        query.myAlbums = new String[albums.length];
        for (int i = 0; i < albums.length; i++) {
            query.myAlbums[i] = albums[i].toLowerCase();
        }
        query.myRestrictedPlaylistId = user.getPlaylistId();
        return query;
    }

    public static FindTrackQuery getForArtist(User user, String[] artists, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        query.myArtists = new String[artists.length];
        for (int i = 0; i < artists.length; i++) {
            query.myArtists[i] = artists[i].toLowerCase();
        }
        query.myRestrictedPlaylistId = user.getPlaylistId();
        return query;
    }

    public static FindTrackQuery getForGenre(User user, String[] genres, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        query.myGenres = new String[genres.length];
        for (int i = 0; i < genres.length; i++) {
            query.myGenres[i] = genres[i].toLowerCase();
        }
        query.myRestrictedPlaylistId = user.getPlaylistId();
        return query;
    }

    private List<String> myIds;
    private String[] myAlbums;
    private String[] myGenres;
    private String[] myArtists;
    private boolean myArtistSort;
    private String myRestrictedPlaylistId;

    private FindTrackQuery() {
        // intentionally left blank
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        if (!CollectionUtils.isEmpty(myIds)) {
            return executeForIds(connection);
        } else {
            SmartStatement statement;
            String suffix = StringUtils.isEmpty(myRestrictedPlaylistId) ? "" : "Restricted";
            if (myArtistSort) {
                statement = MyTunesRssUtils.createStatement(connection, "findTracksWithArtistOrder" + suffix);
            } else {
                statement = MyTunesRssUtils.createStatement(connection, "findTracks" + suffix);
            }
            statement.setItems("album", myAlbums);
            statement.setItems("artist", myArtists);
            statement.setItems("genre", myGenres);
            statement.setString("restrictedPlaylistId", myRestrictedPlaylistId);
            return execute(statement, new TrackResultBuilder());
        }
    }

    private QueryResult<Track> executeForIds(Connection connection) throws SQLException {
            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "preFindTracksByIds");
            statement.setObject("track_id", myIds);
            statement.execute();
            String suffix = StringUtils.isEmpty(myRestrictedPlaylistId) ? "" : "Restricted";
            if (myArtistSort) {
                statement = MyTunesRssUtils.createStatement(connection, "findTracksByIdsWithArtistOrder" + suffix);
            } else {
                statement = MyTunesRssUtils.createStatement(connection, "findTracksByIds" + suffix);
            }
            return execute(statement, new TrackResultBuilder());
    }
}