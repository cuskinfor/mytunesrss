/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.mp3.*;
import org.apache.commons.lang.*;

import javax.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.CreateM3uCommandHandler
 */
public class CreateM3uCommandHandler extends CreatePlaylistCommandHandler {

    @Override
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        Collection<Track> tracks = getTracks();
        if (tracks != null && !tracks.isEmpty()) {
            getRequest().setAttribute("tracks", tracks);
            forward(MyTunesRssResource.TemplateM3u);
        } else {
            addError(new BundleError("error.emptyFeed"));
            forward(MyTunesRssCommand.ShowPortal);// todo: redirect to backUrl
        }
    }

}