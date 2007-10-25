/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.sql.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.*;

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
            FindAlbumQuery findAlbumQuery = new FindAlbumQuery(getAuthUser(), getDisplayFilter().getTextFilter(), artist, genre, getIntegerRequestParameter("page", -1));
            DataStoreQuery.QueryResult<Album> queryResult = getDataStore().executeQuery(findAlbumQuery);
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
            }
            forward(MyTunesRssResource.BrowseAlbum);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}