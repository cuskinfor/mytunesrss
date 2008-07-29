/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssBase64Utils;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Collections;

/**
 * de.codewave.mytunesrss.command.BrowseArtistCommandHandler
 */
public class BrowseArtistCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        if (isSessionAuthorized()) {
            String album = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("album"));
            if (StringUtils.isEmpty(album)) {
                album = null;
            }
            String genre = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("genre"));
            if (StringUtils.isEmpty(genre)) {
                genre = null;
            }
            getRequest().setAttribute("artistPager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));
            FindArtistQuery findArtistQuery = new FindArtistQuery(getAuthUser(),
                                                                  getDisplayFilter().getTextFilter(),
                                                                  album,
                                                                  genre,
                                                                  getIntegerRequestParameter("page", -1));
            DataStoreQuery.QueryResult<Artist> queryResult = getTransaction().executeQuery(findArtistQuery);
            int pageSize = getWebConfig().getEffectivePageSize();
            List<Artist> artists;
            if (pageSize > 0 && queryResult.getResultSize() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(queryResult.getResultSize(), current);
                getRequest().setAttribute("indexPager", pager);
                artists = queryResult.getResults(current * pageSize, pageSize);
            } else {
                artists = queryResult.getResults();
            }
            getRequest().setAttribute("artists", artists);
            DataStoreQuery.QueryResult<Playlist> playlistsQueryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                                            Collections.singletonList(PlaylistType.MyTunes),
                                                                                                                            null, null,
                                                                                                                            false,
                                                                                                                            true));
            getRequest().setAttribute("editablePlaylists", playlistsQueryResult.getResults());
            forward(MyTunesRssResource.BrowseArtist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}