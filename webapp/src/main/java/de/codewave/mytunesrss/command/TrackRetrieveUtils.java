/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.TrackRetrieveUtils
 */
public class TrackRetrieveUtils {
    private static String getRequestParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }

    private static boolean getBooleanRequestParameter(HttpServletRequest request, String name, boolean defaultValue) {
        String value = request.getParameter(name);
        return StringUtils.isNotEmpty(value) ? Boolean.valueOf(value) : defaultValue;
    }

    private static int getIntegerRequestParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        return StringUtils.isNotEmpty(value) ? Integer.valueOf(value) : defaultValue;
    }

    private static void decodeBase64(String[] strings) {
        if (strings != null && strings.length > 0) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = MyTunesRssBase64Utils.decodeToString(strings[i]);
            }
        }
    }

    private static String[] getNonEmptyParameterValues(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        if (values != null && values.length > 0) {
            List<String> nonEmptyValues = new ArrayList<String>();
            for (String value : values) {
                if (StringUtils.isNotEmpty(value)) {
                    nonEmptyValues.add(value);
                }
            }
            return nonEmptyValues.toArray(new String[nonEmptyValues.size()]);
        }
        return null;
    }

    public static DataStoreQuery<DataStoreQuery.QueryResult<Track>> getQuery(DataStoreSession session, HttpServletRequest servletRequest, User user,
            boolean keepPlaylistOrder) throws SQLException {
        String[] albums = getNonEmptyParameterValues(servletRequest, "album");
        decodeBase64(albums);
        String[] artists = getNonEmptyParameterValues(servletRequest, "artist");
        decodeBase64(artists);
        String[] albumArtists = getNonEmptyParameterValues(servletRequest, "albumartist");
        decodeBase64(albumArtists);
        String genre = MyTunesRssBase64Utils.decodeToString(getRequestParameter(servletRequest, "genre", null));
        String playlistId = getRequestParameter(servletRequest, "playlist", null);
        String sortOrderName = getRequestParameter(servletRequest, "sortOrder", SortOrder.Album.name());
        SortOrder sortOrderValue = SortOrder.valueOf(sortOrderName);
        boolean fullAlbums = getBooleanRequestParameter(servletRequest, "fullAlbums", false);
        String series = MyTunesRssBase64Utils.decodeToString(getRequestParameter(servletRequest, "series", null));
        int season = getIntegerRequestParameter(servletRequest, "season", -1);

        if (albums != null && albums.length > 0) {
            return FindTrackQuery.getForAlbum(user, albums, albumArtists != null ? albumArtists : new String[0], sortOrderValue);
        } else if (artists != null && artists.length > 0) {
            if (fullAlbums) {
                Collection<String> albumNames = new HashSet<String>();
                for (String artist : artists) { // full albums should not happen with more than one artist, otherwise this solution would be rather slow
                    FindAlbumQuery findAlbumQuery = new FindAlbumQuery(user, null, artist, null, -1, -1, -1, false, FindAlbumQuery.AlbumType.ALL);
                    DataStoreQuery.QueryResult<Album> albumsWithArtist = session.executeQuery(findAlbumQuery);
                    for (Album albumWithArtist = albumsWithArtist.nextResult(); albumWithArtist != null;
                            albumWithArtist = albumsWithArtist.nextResult()) {
                        albumNames.add(albumWithArtist.getName());
                    }
                }
                return FindTrackQuery.getForAlbum(user,
                                                  albumNames.toArray(new String[albumNames.size()]), new String[0],
                                                  sortOrderValue);
            } else {
                return FindTrackQuery.getForArtist(user, artists, sortOrderValue);
            }
        } else if (StringUtils.isNotEmpty(genre)) {
            if (fullAlbums) {
                FindAlbumQuery findAlbumQuery = new FindAlbumQuery(user, null, null, genre, -1, -1, -1, false, FindAlbumQuery.AlbumType.ALL);
                DataStoreQuery.QueryResult<Album> albumsWithGenre = session.executeQuery(findAlbumQuery);
                List<String> albumNames = new ArrayList<String>();
                for (Album albumWithGenre = albumsWithGenre.nextResult(); albumWithGenre != null; albumWithGenre = albumsWithGenre.nextResult()) {
                    albumNames.add(albumWithGenre.getName());
                }
                return FindTrackQuery.getForAlbum(user,
                                                  albumNames.toArray(new String[albumNames.size()]), new String[0],
                                                  sortOrderValue);
            } else {
                return FindTrackQuery.getForGenre(user, new String[] {genre}, sortOrderValue);
            }
        } else if (StringUtils.isNotEmpty(playlistId)) {
            if (keepPlaylistOrder) {
                return new FindPlaylistTracksQuery(user, playlistId, null);
            } else {
                return new FindPlaylistTracksQuery(user, playlistId, sortOrderValue);
            }
        } else if (series != null) {
            return season > -1 ? FindTrackQuery.getTvShowSeriesSeasonEpisodes(user, series, season) : FindTrackQuery.getTvShowSeriesEpisodes(user, series);
        }
        return null;
    }
}