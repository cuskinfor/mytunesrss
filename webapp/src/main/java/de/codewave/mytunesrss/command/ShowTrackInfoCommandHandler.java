/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.*;

import java.util.*;

import org.apache.commons.io.*;

/**
 * de.codewave.mytunesrss.command.ShowTrackInfoCommandHandler
 */
public class ShowTrackInfoCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        String trackId = getRequest().getParameter("track");
        Collection<Track> tracks = getTransaction().executeQuery(FindTrackQuery.getForId(new String[] {trackId})).getResults();
        if (!tracks.isEmpty()) {
            Track track = tracks.iterator().next();
            getRequest().setAttribute("track", track);
            if (FileSupportUtils.isMp3(track.getFile())) {
                getRequest().setAttribute("mp3info", Boolean.TRUE);
            }
        }
        forward(MyTunesRssResource.TrackInfo);
    }
}