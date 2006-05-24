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
        String artist = getRequest().getParameter("artist");
        String page = getRequest().getParameter("page");
        Collection<Album> albums;
        if (StringUtils.isNotEmpty(page)) {
            albums = getDataStore().executeQuery(new FindAlbumQuery(Integer.parseInt(page)));
        } else {
            albums = getDataStore().executeQuery(new FindAlbumQuery(artist));
        }
        int pageSize = getWebConfig().getPageSize();
        if (pageSize > 0 && albums.size() > pageSize) {
            int current = Integer.parseInt(getRequestParameter("index", "0"));
            Pager pager = createPager(albums.size(), current);
            getRequest().setAttribute("indexPager", pager);
            albums = ((List<Album>)albums).subList(current * pageSize, Math.min((current * pageSize) + pageSize, albums.size()));
        }
        getRequest().setAttribute("albums", albums);
        Boolean singleArtist = Boolean.valueOf(StringUtils.isNotEmpty(artist));
        getRequest().setAttribute("singleArtist", singleArtist);
        if (singleArtist) {
            int singleArtistTrackCount = 0;
            for (Album album : albums) {
                singleArtistTrackCount += album.getTrackCount();
            }
            getRequest().setAttribute("singleArtistTrackCount", singleArtistTrackCount);
        }
        forward(MyTunesRssResource.BrowseAlbum);
    }
}