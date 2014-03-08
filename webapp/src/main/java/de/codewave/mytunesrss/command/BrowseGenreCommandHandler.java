/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.OffHeapSessionStore;
import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import de.codewave.utils.sql.ResultSetType;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.BrowseAlbumCommandHandler
 */
public class BrowseGenreCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws IOException, ServletException, SQLException {
        if (isSessionAuthorized()) {
            String page = getRequest().getParameter("page");
            getRequest().setAttribute("genrePager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));

            OffHeapSessionStore offHeapSessionStore = OffHeapSessionStore.get(getRequest());
            String currentListId = getRequestParameter(OffHeapSessionStore.CURRENT_LIST_ID, null);
            List<Genre> cachedGenres = offHeapSessionStore.getCurrentList(currentListId);

            if (cachedGenres == null)
            {
                FindGenresQuery query;
                if (StringUtils.isNotEmpty(page)) {
                    query = new FindGenresQuery(getAuthUser(), false, Integer.parseInt(page));
                } else {
                    query = new FindGenresQuery(getAuthUser(), false, -1);
                }
                query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
                currentListId = offHeapSessionStore.newCurrentList();
                cachedGenres = offHeapSessionStore.getCurrentList(currentListId);
                getTransaction().executeQuery(query).addRemainingResults(cachedGenres);
            }
            getRequest().setAttribute(OffHeapSessionStore.CURRENT_LIST_ID, currentListId);
            int pageSize = getWebConfig().getEffectivePageSize();
            List<Genre> genres;
            if (pageSize > 0 && cachedGenres.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(cachedGenres.size(), current);
                getRequest().setAttribute("indexPager", pager);
                genres = MyTunesRssUtils.getSubList(cachedGenres, current * pageSize, pageSize);
            } else {
                genres = cachedGenres;
            }
            getRequest().setAttribute("genres", genres);
            QueryResult<Playlist> playlistsQueryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                                            Collections.singletonList(
                                                                                                                                    PlaylistType.MyTunes),
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            false,
                                                                                                                            true));
            getRequest().setAttribute("editablePlaylists", playlistsQueryResult.getResults());
            forward(MyTunesRssResource.BrowseGenre);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
