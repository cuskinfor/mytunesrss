/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.datastore.statement.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;

/**
 * de.codewave.mytunesrss.command.BrowseAlbumCommandHandler
 */
public class BrowseAlbumCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws IOException, ServletException, SQLException {
        String artist = getRequestParameter("artist", "%");
        getRequest().setAttribute("albums", getDataStore().executeQuery(new FindAlbumQuery(artist)));
        forward(MyTunesRssResource.BrowseAlbum);
    }
}