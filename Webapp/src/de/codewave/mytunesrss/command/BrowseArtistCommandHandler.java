/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.jsp.*;

import javax.servlet.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.command.BrowseArtistCommandHandler
 */
public class BrowseArtistCommandHandler extends MyTunesRssCommandHandler {
    public void executeAuthenticated() throws IOException, ServletException {
        String album = getRequestParameter("album", "%");
        getRequest().setAttribute("artists", getDataStore().findArtistsByAlbum(album));
        forward(MyTunesRssResource.BrowseArtist);
    }
}