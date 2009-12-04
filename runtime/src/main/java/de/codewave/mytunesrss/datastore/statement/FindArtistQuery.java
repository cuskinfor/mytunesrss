/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindArtistQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Artist>> {
    private String myFilter;
    private String myAlbum;
    private String myGenre;
    private int myIndex;
    private List<String> myRestrictedPlaylistIds = Collections.emptyList();;

    public FindArtistQuery(User user, String filter, String album, String genre, int index) {
        myFilter = StringUtils.isNotEmpty(filter) ? "%" + filter + "%" : null;
        myAlbum = album;
        myGenre = genre;
        myIndex = index;
        myRestrictedPlaylistIds = user.getPlaylistIds();
    }

    public QueryResult<Artist> execute(Connection connection) throws SQLException {
        Map<String, Boolean> conditionals = new HashMap<String, Boolean>();
        conditionals.put("index", MyTunesRssUtils.isLetterPagerIndex(myIndex));
        conditionals.put("track", StringUtils.isNotBlank(myAlbum) || StringUtils.isNotBlank(myGenre) || !myRestrictedPlaylistIds.isEmpty());
        conditionals.put("filter", StringUtils.isNotBlank(myFilter));
        conditionals.put("artist", StringUtils.isNotBlank(myAlbum));
        conditionals.put("genre", StringUtils.isNotBlank(myGenre));
        conditionals.put("restricted", !myRestrictedPlaylistIds.isEmpty());
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findArtists", conditionals);
        statement.setString("filter", myFilter);
        statement.setString("album", myAlbum);
        statement.setString("genre", myGenre);
        statement.setInt("index", myIndex);
        statement.setItems("restrictedPlaylistIds", myRestrictedPlaylistIds);
        return execute(statement, new ArtistResultBuilder());
    }

    public static class ArtistResultBuilder implements ResultBuilder<Artist> {
        private ArtistResultBuilder() {
            // intentionally left blank
        }

        public Artist create(ResultSet resultSet) throws SQLException {
            Artist artist = new Artist();
            artist.setName(resultSet.getString("NAME"));
            artist.setAlbumCount(resultSet.getInt("ALBUM_COUNT"));
            artist.setTrackCount(resultSet.getInt("TRACK_COUNT"));
            return artist;
        }
    }
}