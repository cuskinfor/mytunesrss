/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
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
            String genre = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("genre"));
            getRequest().setAttribute("albumPager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));
            FindAlbumQuery findAlbumQuery = new FindAlbumQuery(artist, genre, getIntegerRequestParameter("page", -1));
            Collection<Album> albums = getDataStore().executeQuery(findAlbumQuery);
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && albums.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(albums.size(), current);
                getRequest().setAttribute("indexPager", pager);
                albums = ((List<Album>)albums).subList(current * pageSize, Math.min((current * pageSize) + pageSize, albums.size()));
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