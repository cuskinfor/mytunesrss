/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.Pager;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;

import java.util.Arrays;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.ShowPlaylistManagerCommandHandler
 */
public class ShowPlaylistManagerCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (getAuthUser().isCreatePlaylists()) {
            QueryResult<Playlist> queryResult = getTransaction().executeQuery(FindPlaylistQuery.createForPlaylistManager(getAuthUser()));
            int pageSize = getWebConfig().getEffectivePageSize();
            List<Playlist> playlists;
            if (pageSize > 0 && queryResult.getResultSize() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(queryResult.getResultSize(), current);
                getRequest().setAttribute("pager", pager);
                playlists = queryResult.getResults(current * pageSize, pageSize);
            } else {
                playlists = queryResult.getResults();
            }
            getRequest().setAttribute("playlists", playlists);
            forward(MyTunesRssResource.PlaylistManager);
        } else {
            forward(MyTunesRssCommand.Login);
        }
    }
}