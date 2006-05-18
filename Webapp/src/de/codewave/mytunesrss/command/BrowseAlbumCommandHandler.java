/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

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
public class BrowseAlbumCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws IOException, ServletException, SQLException {
        String artist = getRequest().getParameter("artist");
        String page = getRequest().getParameter("page");
        Collection<Album> albums;
        if (StringUtils.isNotEmpty(page)) {
            List<String> startPatterns = new ArrayList<String>();
            for (StringTokenizer tokenizer = new StringTokenizer(page, "_"); tokenizer.hasMoreTokens();) {
                startPatterns.add(tokenizer.nextToken().toLowerCase() + "%");
            }
            albums = getDataStore().executeQuery(new FindAlbumQuery(startPatterns.toArray(new String[startPatterns.size()])));
        } else {
            albums = getDataStore().executeQuery(new FindAlbumQuery(artist));
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
        createPager();
        forward(MyTunesRssResource.BrowseAlbum);
    }
}