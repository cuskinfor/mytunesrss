/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;

import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.BrowseAlbumCommandHandler
 */
public class BrowseAlbumCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthenticated() throws IOException, ServletException {
        String artist = getRequestParameter("artist", "%");
        getRequest().setAttribute("albums", getDataStore().findAlbumsByArtist(artist));
        forward(MyTunesRssResource.BrowseAlbum);
    }
}