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
        if (isSessionAuthorized()) {
            String album = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("album"));
            String genre = MyTunesRssBase64Utils.decodeToString(getRequest().getParameter("genre"));
            getRequest().setAttribute("artistPager", new Pager(PagerConfig.PAGES, PagerConfig.PAGES.size()));
            FindArtistQuery findArtistQuery = new FindArtistQuery(getAuthUser(), album, genre, getIntegerRequestParameter("page", -1));
            Collection<Artist> artists = getDataStore().executeQuery(findArtistQuery);
            int pageSize = getWebConfig().getEffectivePageSize();
            if (pageSize > 0 && artists.size() > pageSize) {
                int current = getSafeIntegerRequestParameter("index", 0);
                Pager pager = createPager(artists.size(), current);
                getRequest().setAttribute("indexPager", pager);
                artists = ((List<Artist>)artists).subList(current * pageSize, Math.min((current * pageSize) + pageSize, artists.size()));
            }
            getRequest().setAttribute("artists", artists);
            forward(MyTunesRssResource.BrowseArtist);
        } else {
            forward(MyTunesRssResource.Login);
        }
    }
}