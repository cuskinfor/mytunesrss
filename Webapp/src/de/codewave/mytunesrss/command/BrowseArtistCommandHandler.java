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
 * de.codewave.mytunesrss.command.BrowseArtistCommandHandler
 */
public class BrowseArtistCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        String album = getRequest().getParameter("album");
        String page = getRequest().getParameter("page");
        List<Pager.Page> artistPages = (List<Pager.Page>)getDataStore().executeQuery(new FindPagesQuery(InsertPageStatement.PagerType.Artist));
        if (artistPages != null) {
            getRequest().setAttribute("artistPager", new Pager(artistPages, artistPages.size()));
        }
        Collection<Artist> artists;
        if (StringUtils.isNotEmpty(page)) {
            artists = getDataStore().executeQuery(new FindArtistQuery(Integer.parseInt(page)));
        } else {
            artists = getDataStore().executeQuery(new FindArtistQuery(album));
        }
        int pageSize = getWebConfig().getEffectivePageSize();
        if (pageSize > 0 && artists.size() > pageSize) {
            int current = Integer.parseInt(getRequestParameter("index", "0"));
            Pager pager = createPager(artists.size(), current);
            getRequest().setAttribute("indexPager", pager);
            artists = ((List<Artist>)artists).subList(current * pageSize, Math.min((current * pageSize) + pageSize, artists.size()));
        }
        getRequest().setAttribute("artists", artists);
        forward(MyTunesRssResource.BrowseArtist);
    }
}