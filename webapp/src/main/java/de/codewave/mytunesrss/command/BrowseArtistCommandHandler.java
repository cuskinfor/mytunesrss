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
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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
            OffHeapSessionStore offHeapSessionStore = OffHeapSessionStore.get(getRequest());
            String currentListId = getRequestParameter(OffHeapSessionStore.CURRENT_LIST_ID, null);
            List<Artist> cachedArtists = offHeapSessionStore.getCurrentList(currentListId);
            if (cachedArtists == null) {
                FindArtistQuery findArtistQuery = new FindArtistQuery(getAuthUser(),
                                                                      getDisplayFilter().getTextFilter(),
                                                                      album,
                                                                      genre,
                                                                      getIntegerRequestParameter("page", -1));
                findArtistQuery.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
                currentListId = offHeapSessionStore.newCurrentList();
                cachedArtists = offHeapSessionStore.getCurrentList(currentListId);
                getTransaction().executeQuery(findArtistQuery).addRemainingResults(cachedArtists);
            }
            getRequest().setAttribute(OffHeapSessionStore.CURRENT_LIST_ID, currentListId);
            int pageSize = getWebConfig().getEffectivePageSize();
            List<Artist> artists;
            if (pageSize > 0 && cachedArtists.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(cachedArtists.size(), current);
                getRequest().setAttribute("indexPager", pager);
                artists = MyTunesRssUtils.getSubList(cachedArtists, current * pageSize, pageSize);
            } else {
                artists = cachedArtists;
            }
            getRequest().setAttribute("artists", artists);
            DataStoreQuery.QueryResult<Playlist> playlistsQueryResult = getTransaction().executeQuery(new FindPlaylistQuery(getAuthUser(),
                                                                                                                            Collections.singletonList(
                                                                                                                                    PlaylistType.MyTunes),
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            false,
                                                                                                                            true));
            getRequest().setAttribute("editablePlaylists", playlistsQueryResult.getResults());
            forward(MyTunesRssResource.BrowseArtist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}
