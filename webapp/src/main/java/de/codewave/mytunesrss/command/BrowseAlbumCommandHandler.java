/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.OffHeapSessionStore;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.BrowseAlbumCommandHandler
 */
public class BrowseAlbumCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws IOException, ServletException, SQLException {
        if (isSessionAuthorized()) {
            String artist = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("artist"));
            if (StringUtils.isEmpty(artist)) {
                artist = null;
            }
            String genre = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("genre"));
            if (StringUtils.isEmpty(genre)) {
                genre = null;
            }
            getRequest().setAttribute("albumPager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));
            OffHeapSessionStore offHeapSessionStore = OffHeapSessionStore.get(getRequest());
            String currentListId = getRequestParameter(OffHeapSessionStore.CURRENT_LIST_ID, null);
            List<Album> cachedAlbums = offHeapSessionStore.getCurrentList(currentListId);
            if (cachedAlbums == null) {
                FindAlbumQuery findAlbumQuery = new FindAlbumQuery(getAuthUser(),
                                                                   getDisplayFilter().getTextFilter(),
                                                                   artist,
                                                                   false,
                                                                   genre,
                                                                   getIntegerRequestParameter("page", -1),
                                                                   getDisplayFilter().getMinYear(),
                                                                   getDisplayFilter().getMaxYear(),
                                                                   getBooleanRequestParameter("sortByYear", false),
                                                                   StringUtils.isNotBlank(artist),
                                                                   getDisplayFilter().getAlbumType());
                findAlbumQuery.setResultSetType(ResultSetType.TYPE_FORWARD_ONLY);
                findAlbumQuery.setFetchSize(1000);
                DataStoreQuery.QueryResult<Album> queryResult = getTransaction().executeQuery(findAlbumQuery);
                currentListId = offHeapSessionStore.newCurrentList();
                for (Album album = queryResult.nextResult(); album != null; album = queryResult.nextResult()) {
                    offHeapSessionStore.addToCurrentList(album);
                }
                cachedAlbums = offHeapSessionStore.getCurrentList(currentListId);
            }
            getRequest().setAttribute(OffHeapSessionStore.CURRENT_LIST_ID, currentListId);
            int pageSize = getWebConfig().getEffectivePageSize();
            List<Album> albums = new ArrayList<Album>();
            int trackCount = 0;
            int current = getSafeIntegerRequestParameter("index", 0);
            if (pageSize > 0 && cachedAlbums.size() > pageSize) {
                Pager pager = createPager(cachedAlbums.size(), current);
                getRequest().setAttribute("indexPager", pager);
            }
            int i = 0;
            for (Album album : cachedAlbums) {
                trackCount += album.getTrackCount();
                if (pageSize == 0 || (i >= current * pageSize && i < (current + 1) * pageSize)) {
                    albums.add(album);
                }
                i++;
            }
            getRequest().setAttribute("albums", albums);
            Boolean singleGenre = Boolean.valueOf(StringUtils.isNotEmpty(genre));
            Boolean singleArtist = Boolean.valueOf(StringUtils.isNotEmpty(artist));
            getRequest().setAttribute("singleGenre", singleGenre);
            getRequest().setAttribute("singleArtist", singleArtist);
            if (singleArtist || singleGenre) {
                getRequest().setAttribute("allAlbumsTrackCount", trackCount);
                if (singleArtist) {
                    final String finalArtist = artist;
                    getRequest().setAttribute("allArtistGenreTrackCount", getTransaction().executeQuery(new DataStoreQuery<Object>() {
                        public Object execute(Connection connection) throws SQLException {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findArtistTrackCount");
                            statement.setString("name", finalArtist);
                            ResultSet rs = statement.executeQuery();
                            if (rs.next()) {
                                return rs.getInt("COUNT");
                            }
                            return Long.valueOf(0);
                        }
                    }));
                } else {
                    final String finalGenre = genre;
                    getRequest().setAttribute("allArtistGenreTrackCount", getTransaction().executeQuery(new DataStoreQuery<Object>() {
                        public Object execute(Connection connection) throws SQLException {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findGenreTrackCount");
                            statement.setString("name", finalGenre);
                            ResultSet rs = statement.executeQuery();
                            if (rs.next()) {
                                return rs.getInt("COUNT");
                            }
                            return Long.valueOf(0);
                        }
                    }));
                }
            }
            DataStoreQuery.QueryResult<Playlist> playlistsQueryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                                            Collections.singletonList(
                                                                                                                                    PlaylistType.MyTunes),
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            false,
                                                                                                                            true));
            getRequest().setAttribute("editablePlaylists", playlistsQueryResult.getResults());
            forward(MyTunesRssResource.BrowseAlbum);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }

}
