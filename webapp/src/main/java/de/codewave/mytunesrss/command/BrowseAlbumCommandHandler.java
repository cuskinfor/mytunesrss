/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            FindAlbumQuery findAlbumQuery = new FindAlbumQuery(getAuthUser(),
                                                               getDisplayFilter().getTextFilter(),
                                                               artist,
                                                               genre,
                                                               getIntegerRequestParameter("page", -1),
                                                               getIntegerRequestParameter("minyear", -1),
                                                               getIntegerRequestParameter("maxyear", -1));
            DataStoreQuery.QueryResult<Album> queryResult = getTransaction().executeQuery(findAlbumQuery);
            int pageSize = getWebConfig().getEffectivePageSize();
            List<Album> albums;
            if (pageSize > 0 && queryResult.getResultSize() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(queryResult.getResultSize(), current);
                getRequest().setAttribute("indexPager", pager);
                albums = queryResult.getResults(current * pageSize, pageSize);
            } else {
                albums = queryResult.getResults();
            }
            getRequest().setAttribute("albums", albums);
            Boolean singleGenre = Boolean.valueOf(StringUtils.isNotEmpty(genre));
            Boolean singleArtist = Boolean.valueOf(StringUtils.isNotEmpty(artist));
            getRequest().setAttribute("singleGenre", singleGenre);
            getRequest().setAttribute("singleArtist", singleArtist);
            if (singleArtist || singleGenre) {
                int trackCount = 0;
                for (Album album : albums) {
                    trackCount += album.getTrackCount();
                }
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
                            return new Long(0);
                        }
                    }));
                } else {
                    final String finalArtist = artist;
                    getRequest().setAttribute("allArtistGenreTrackCount", getTransaction().executeQuery(new DataStoreQuery<Object>() {
                        public Object execute(Connection connection) throws SQLException {
                            SmartStatement statement = MyTunesRssUtils.createStatement(connection, "findGenreTrackCount");
                            statement.setString("name", finalArtist);
                            ResultSet rs = statement.executeQuery();
                            if (rs.next()) {
                                return rs.getInt("COUNT");
                            }
                            return new Long(0);
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