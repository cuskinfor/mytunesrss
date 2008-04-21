/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.FileSupportUtils;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.camel.mp3.Mp3Info;
import de.codewave.camel.mp3.Mp3Utils;

import java.util.Collection;
import java.io.FileInputStream;

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
                Mp3Info info = Mp3Utils.getMp3Info(new FileInputStream(track.getFile()));
                getRequest().setAttribute("avgBitRate", info.getAvgBitrate());
                getRequest().setAttribute("avgSampleRate", info.getAvgSampleRate());
            }
        }
        forward(MyTunesRssResource.TrackInfo);
    }
}