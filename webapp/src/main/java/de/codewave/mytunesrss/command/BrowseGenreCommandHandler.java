/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.datastore.statement.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.command.BrowseAlbumCommandHandler
 */
public class BrowseGenreCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws IOException, ServletException, SQLException {
        if (isSessionAuthorized()) {
            String page = getRequest().getParameter("page");
            getRequest().setAttribute("genrePager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));
            Collection<Genre> genres;
            if (StringUtils.isNotEmpty(page)) {
                genres = getDataStore().executeQuery(new FindGenreQuery(getAuthUser(), Integer.parseInt(page)));
            } else {
                genres = getDataStore().executeQuery(new FindGenreQuery(getAuthUser(), -1));
            }
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && genres.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(genres.size(), current);
                getRequest().setAttribute("indexPager", pager);
                genres = ((List<Genre>)genres).subList(current * pageSize, Math.min((current * pageSize) + pageSize, genres.size()));
            }
            getRequest().setAttribute("genres", genres);
            forward(MyTunesRssResource.BrowseGenre);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}