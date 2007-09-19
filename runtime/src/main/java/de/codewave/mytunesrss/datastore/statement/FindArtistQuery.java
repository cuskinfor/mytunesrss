/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.*;
import de.codewave.utils.sql.*;

import java.sql.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.datastore.statement.FindAlbumQuery
 */
public class FindArtistQuery extends DataStoreQuery<Collection<Artist>> {
    private String myAlbum;
    private String myGenre;
    private int myIndex;
    private String myRestrictedPlaylistId;

    public FindArtistQuery(User user, String album, String genre, int index) {
        myAlbum = album;
        myGenre = genre;
        myIndex = index;
        myRestrictedPlaylistId = user.getPlaylistId();
    }

    public Collection<Artist> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findArtists" + (StringUtils.isEmpty(myRestrictedPlaylistId) ? "" : "Restricted"));
        statement.setString("album", myAlbum);
        statement.setString("genre", myGenre);
        statement.setInt("index", myIndex);
        statement.setString("restrictedPlaylistId", myRestrictedPlaylistId);
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