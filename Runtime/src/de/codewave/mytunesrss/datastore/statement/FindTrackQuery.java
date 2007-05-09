/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultBuilder;
import de.codewave.utils.sql.SQLUtils;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.mytunesrss.datastore.statement.FindTrackQueryry
 */
public class FindTrackQuery extends DataStoreQuery<Collection<Track>> {
  public static FindTrackQuery getForId(String[] trackIds) {
    FindTrackQuery query = new FindTrackQuery();
    query.myIds = trackIds;
    return query;
  }


  public static FindTrackQuery getForSearchTerm(String searchTerm, boolean sortByArtistFirst) {
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
    return query;
  }

  public static FindTrackQuery getForAlbum(String[] albums, boolean sortByArtistFirst) {
    FindTrackQuery query = new FindTrackQuery();
    query.myArtistSort = sortByArtistFirst;
    query.myAlbums = albums;
    return query;
  }

  public static FindTrackQuery getForArtist(String[] artists, boolean sortByArtistFirst) {
    FindTrackQuery query = new FindTrackQuery();
    query.myArtistSort = sortByArtistFirst;
    query.myArtists = artists;
    return query;
  }

  public static FindTrackQuery getForGenre(String[] genres, boolean sortByArtistFirst) {
    FindTrackQuery query = new FindTrackQuery();
    query.myArtistSort = sortByArtistFirst;
    query.myGenres = genres;
    return query;
  }

  private String[] myIds;
  private String[] myAlbums;
  private String[] myGenres;
  private String[] myArtists;
  private String[] mySearchTerms;
  private boolean myArtistSort;


  private FindTrackQuery() {
    // intentionally left blank
  }

  public Collection<Track> execute(Connection connection) throws SQLException {
    SmartStatement statement;
    if (myArtistSort) {
      statement = MyTunesRssUtils.createStatement(connection, "findTracksWithArtistOrder");
    } else {
      statement = MyTunesRssUtils.createStatement(connection, "findTracks");
    }
    statement.setObject("id", myIds != null && myIds.length > 0 ? Arrays.asList(myIds) : null);
    statement.setInt("size_id", myIds != null ? myIds.length : 0);
    statement.setObject("album", myAlbums != null && myAlbums.length > 0 ? Arrays.asList(myAlbums) : null);
    statement.setInt("size_album", myAlbums != null ? myAlbums.length : 0);
    statement.setObject("artist", myArtists != null && myArtists.length > 0 ? Arrays.asList(myArtists) : null);
    statement.setInt("size_artist", myArtists != null ? myArtists.length : 0);
    statement.setObject("genre", myGenres != null && myGenres.length > 0 ? Arrays.asList(myGenres) : null);
    statement.setInt("size_genre", myGenres != null ? myGenres.length : 0);
    statement.setObject("search", mySearchTerms != null && mySearchTerms.length > 0 ? Arrays.asList(mySearchTerms) : null);
    Collection<Track> tracks = execute(statement, new TrackResultBuilder());
    if (myIds != null && myIds.length > 1) {
      Map<String, Track> idToTrack = new HashMap<String, Track>(tracks.size());
      for (Track track : tracks) {
        idToTrack.put(track.getId(), track);
      }
      tracks.clear();
      for (int i = 0; i < myIds.length; i++) {
        tracks.add(idToTrack.get(myIds[i]));
      }
    }
    return tracks;
  }

  public static class TrackResultBuilder implements ResultBuilder<Track> {
    private TrackResultBuilder() {
      // intentionally left blank
    }

    public Track create(ResultSet resultSet) throws SQLException {
      Track track = new Track();
      track.setId(resultSet.getString("ID"));
      track.setName(resultSet.getString("NAME"));
      track.setArtist(resultSet.getString("ARTIST"));
      track.setAlbum(resultSet.getString("ALBUM"));
      track.setTime(resultSet.getInt("TIME"));
      track.setTrackNumber(resultSet.getInt("TRACK_NUMBER"));
      String pathname = resultSet.getString("FILE");
      track.setFile(StringUtils.isNotEmpty(pathname) ? new File(pathname) : null);
      track.setProtected(resultSet.getBoolean("PROTECTED"));
      track.setVideo(resultSet.getBoolean("VIDEO"));
      track.setGenre(resultSet.getString("GENRE"));
      return track;
    }
  }
}