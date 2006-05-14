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
 * de.codewave.mytunesrss.command.BrowseArtistCommandHandler
 */
public class BrowseArtistCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        String album = getRequest().getParameter("album");
        getRequest().setAttribute("artists", getDataStore().executeQuery(new FindArtistQuery(album)));
        forward(MyTunesRssResource.BrowseArtist);
    }
}