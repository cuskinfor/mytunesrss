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
 * de.codewave.mytunesrss.command.BrowseArtistCommandHandler
 */
public class BrowseArtistCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        String album = getRequest().getParameter("album");
        String page = getRequest().getParameter("page");
        Collection<Artist> artists;
        if (StringUtils.isNotEmpty(page)) {
            List<String> startPatterns = new ArrayList<String>();
            for (StringTokenizer tokenizer = new StringTokenizer(page, "_"); tokenizer.hasMoreTokens();) {
                startPatterns.add(tokenizer.nextToken().toLowerCase() + "%");
            }
            artists = getDataStore().executeQuery(new FindArtistQuery(startPatterns.toArray(new String[startPatterns.size()])));
        } else {
            artists = getDataStore().executeQuery(new FindArtistQuery(album));
        }
        getRequest().setAttribute("artists", artists);
        createPager();
        forward(MyTunesRssResource.BrowseArtist);
    }
}