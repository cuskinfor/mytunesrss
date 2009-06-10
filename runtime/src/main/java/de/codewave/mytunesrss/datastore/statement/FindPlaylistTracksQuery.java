/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.MediaType;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindPlaylistTracksQuery extends DataStoreQuery<DataStoreQuery.QueryResult<Track>> {
    public static final String PSEUDO_ID_ALL_BY_ARTIST = "PlaylistAllByArtist";
    public static final String PSEUDO_ID_ALL_BY_ALBUM = "PlaylistAllByAlbum";
    public static final String PSEUDO_ID_RANDOM = "PlaylistRandom";
    public static final String PSEUDO_ID_MOST_PLAYED = "PlaylistMostPlayed";
    public static final String PSEUDO_ID_LAST_UPDATED = "PlaylistLastUpdated";

    public static enum SortOrder {
        Album(), Artist(), KeepOrder()
    }

    private String myId;
    private SortOrder mySortOrder;
    private String myRestrictionPlaylistId;

    public FindPlaylistTracksQuery(String id, SortOrder sortOrder) {
        myId = id;
        mySortOrder = sortOrder;
    }

    public FindPlaylistTracksQuery(User user, String id, SortOrder sortOrder) {
        this(id, sortOrder);
        myRestrictionPlaylistId = user.getPlaylistId();
    }

    public QueryResult<Track> execute(Connection connection) throws SQLException {
        SmartStatement statement;
        String suffix = StringUtils.isEmpty(myRestrictionPlaylistId) || myRestrictionPlaylistId.equals(myId) ? "" : "Restricted";
        if (PSEUDO_ID_ALL_BY_ALBUM.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracksOrderedByAlbum" + suffix);
            myId = null;
        } else if (PSEUDO_ID_ALL_BY_ARTIST.equals(myId)) {
            statement = MyTunesRssUtils.createStatement(connection, "findAllTracksOrderedByArtist" + suffix);
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_LAST_UPDATED)) {
            statement = MyTunesRssUtils.createStatement(connection, "findLastUpdatedTracks" + suffix);
            String[] splitted = myId.split("_");
            statement.setInt("maxCount", Integer.parseInt(splitted[1]));
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_MOST_PLAYED)) {
            statement = MyTunesRssUtils.createStatement(connection, "findMostPlayedTracks" + suffix);
            String[] splitted = myId.split("_");
            statement.setInt("maxCount", Integer.parseInt(splitted[1]));
            myId = null;
        } else if (myId.startsWith(PSEUDO_ID_RANDOM)) {
            statement = MyTunesRssUtils.createStatement(connection, "findRandomTracks" + suffix);
            String[] splitted = myId.split("_", 4);
            statement.setString("mediatype", StringUtils.trimToNull(splitted[1].split("-", 2)[0]));
            statement.setBoolean("protected", "p".equals(splitted[1].split("-", 2)[1]));
            statement.setInt("maxCount", Integer.parseInt(splitted[2]));
            if (splitted.length > 3) {
                String sourceId = splitted[3];
                QueryResult<Playlist> playlists = new FindPlaylistQuery(null, sourceId, null, false).execute(connection);
                if (playlists.getResultSize() == 1) {
                    Playlist playlist = playlists.nextResult();
                    statement.setString("sourcePlaylistId", StringUtils.trimToNull(playlist.getId()));
                }
            }
            myId = null;
        } else if (mySortOrder == SortOrder.Album) {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByAlbum" + suffix);
        } else if (mySortOrder == SortOrder.Artist) {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByArtist" + suffix);
        } else {
            statement = MyTunesRssUtils.createStatement(connection, "findPlaylistTracksOrderedByIndex" + suffix);
        }
        if (myId != null) {
            String[] parts = StringUtils.split(myId, "@");
            statement.setString("id", parts[0]);
            if (parts.length == 3) {
                statement.setInt("firstIndex", Integer.parseInt(parts[1]));
                statement.setInt("lastIndex", Integer.parseInt(parts[2]));
            }
        }
        statement.setString("restrictedPlaylistId", myRestrictionPlaylistId);
        return execute(statement, new TrackResultBuilder());
    }
}