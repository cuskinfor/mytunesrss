/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.utils.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.BrowseArtistCommandHandler
 */
public class BrowseArtistCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        String album = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("album"));
        String genre = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("genre"));
        String page = getRequest().getParameter("page");
        getRequest().setAttribute("artistPager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));
        Collection<Artist> artists;
        if (StringUtils.isNotEmpty(page)) {
            artists = getDataStore().executeQuery(FindArtistQuery.getForPagerIndex(Integer.parseInt(page)));
        } else if (StringUtils.isNotEmpty(album)) {
            artists = getDataStore().executeQuery(FindArtistQuery.getForAlbum(album));
        } else if (StringUtils.isNotEmpty(genre)) {
            artists = getDataStore().executeQuery(FindArtistQuery.getForGenre(genre));
        } else {
            artists = getDataStore().executeQuery(FindArtistQuery.getAll());
        }
        int pageSize = getWebConfig().getEffectivePageSize();
        if (pageSize > 0 && artists.size() > pageSize) {
            int current = getSafeIntegerRequestParameter("index", 0);
            Pager pager = createPager(artists.size(), current);
            getRequest().setAttribute("indexPager", pager);
            artists = ((List<Artist>)artists).subList(current * pageSize, Math.min((current * pageSize) + pageSize, artists.size()));
        }
        getRequest().setAttribute("artists", artists);
        forward(MyTunesRssResource.BrowseArtist);
    }
}