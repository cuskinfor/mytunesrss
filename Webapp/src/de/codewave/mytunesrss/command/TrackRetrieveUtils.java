/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.TrackRetrieveUtils
 */
public class TrackRetrieveUtils {
    public static enum SortOrder {
        Album(), Artist();
    }

    private static String getRequestParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }

    private static boolean getBooleanRequestParameter(HttpServletRequest request, String name, boolean defaultValue) {
        String value = request.getParameter(name);
        return StringUtils.isNotEmpty(value) ? Boolean.valueOf(value) : defaultValue;
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

    public static DataStoreQuery<Collection<Track>> getQuery(HttpServletRequest servletRequest, boolean keepPlaylistOrder) throws SQLException {
        DataStore store = (DataStore)servletRequest.getSession().getServletContext().getAttribute(MyTunesRssDataStore.class.getName());
        String[] albums = getNonEmptyParameterValues(servletRequest, "album");
        decodeBase64(albums);
        String[] artists = getNonEmptyParameterValues(servletRequest, "artist");
        decodeBase64(artists);
        String genre = MyTunesRssBase64Utils.decodeToString(getRequestParameter(servletRequest, "genre", null));
        String playlistId = getRequestParameter(servletRequest, "playlist", null);
        String sortOrderName = getRequestParameter(servletRequest, "sortOrder", SortOrder.Album.name());
        SortOrder sortOrderValue = SortOrder.valueOf(sortOrderName);
        boolean fullAlbums = getBooleanRequestParameter(servletRequest, "fullAlbums", false);

        if (albums != null && albums.length > 0) {
            return FindTrackQuery.getForAlbum(albums, sortOrderValue == SortOrder.Artist);
        } else if (artists != null && artists.length > 0) {
            if (fullAlbums) {
                Collection<String> albumNames = new HashSet<String>();
                for (String artist : artists) { // full albums should not happen with more than one artist, otherwise this solution would be rather slow
                    FindAlbumQuery findAlbumQuery = new FindAlbumQuery(artist, null, -1);
                    Collection<Album> albumsWithArtist = store.executeQuery(findAlbumQuery);
                    for (Album albumWithArtist : albumsWithArtist) {
                        albumNames.add(albumWithArtist.getName());
                    }
                }
                return FindTrackQuery.getForAlbum(albumNames.toArray(new String[albumNames.size()]), sortOrderValue == SortOrder
                        .Artist);
            } else {
                return FindTrackQuery.getForArtist(artists, sortOrderValue == SortOrder.Artist);
            }
        } else if (StringUtils.isNotEmpty(genre)) {
            if (fullAlbums) {
                FindAlbumQuery findAlbumQuery = new FindAlbumQuery(null, genre, -1);
                Collection<Album> albumsWithGenre = store.executeQuery(findAlbumQuery);
                List<String> albumNames = new ArrayList<String>();
                for (Album albumWithGenre : albumsWithGenre) {
                    albumNames.add(albumWithGenre.getName());
                }
                return FindTrackQuery.getForAlbum(albumNames.toArray(new String[albumNames.size()]), sortOrderValue == SortOrder
                        .Artist);
            } else {
                return FindTrackQuery.getForGenre(new String[] {genre}, sortOrderValue == SortOrder.Artist);
            }
        } else if (StringUtils.isNotEmpty(playlistId)) {
            if (keepPlaylistOrder) {
                return new FindPlaylistTracksQuery(playlistId);
            } else {
                return new FindPlaylistTracksQuery(playlistId, sortOrderValue == SortOrder.Artist);
            }
        }
        return null;
    }
}