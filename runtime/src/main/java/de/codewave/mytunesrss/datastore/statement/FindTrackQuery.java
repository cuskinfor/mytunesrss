/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.SQLUtils;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindTrackQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Track>> {

    public static FindTrackQuery getForId(String[] trackIds) {
        FindTrackQuery query = new FindTrackQuery();
        query.myIds = trackIds;
        return query;
    }


    public static FindTrackQuery getForSearchTerm(User user, String searchTerm, boolean sortByArtistFirst) {
        FindTrackQuery query = new FindTrackQuery();
        query.myArtistSort = sortByArtistFirst;
        String[] searchTerms = StringUtils.split(searchTerm, " ");
        if (searchTerms == null) {
            searchTerms = new String[]{searchTerm};
        }
        for (int i = 0; i < searchTerms.length; i++) {
            if (StringUtils.isNotEmpty(searchTerms[i])) {
                searchTerms[i] = "%" + SQLUtils.escapeLikeString(searchTerms[i].toLowerCase(), "\\") + "%";
            } else {
                searchTerms[i] = "%";
            }
        }
        query.mySearchTerms = searchTerms;
        query.myRestrictedPlaylistId = user.getPlaylistId();
        return query;
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

    private String[] myIds;
    private String[] myAlbums;
    private String[] myGenres;
    private String[] myArtists;
    private String[] mySearchTerms;
    private boolean myArtistSort;
    private String myRestrictedPlaylistId;

    private FindTrackQuery() {
        // intentionally left blank
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        String suffix = StringUtils.isEmpty(myRestrictedPlaylistId) ? "" : "Restricted";
        if (myArtistSort) {
            statement = MyTunesRssUtils.createStatement(connection, "findTracksWithArtistOrder" + suffix);
        } else {
            statement = MyTunesRssUtils.createStatement(connection, "findTracks" + suffix);
        }
        statement.setItems("id", myIds);
        statement.setItems("album", myAlbums);
        statement.setItems("artist", myArtists);
        statement.setItems("genre", myGenres);
        statement.setItems("search", mySearchTerms);
        statement.setString("restrictedPlaylistId", myRestrictedPlaylistId);
        return execute(statement, new TrackResultBuilder());
    }
}