/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.jsp.*;

import javax.servlet.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.ShowPortalCommandHandler
 */
public class ShowPortalCommandHandler extends MyTunesRssCommandHandler {
    public void execute() throws IOException, ServletException {
        List<Playlist> playlists = new ArrayList<Playlist>();
        for (Playlist playlist : getDataStore().findPlaylists()) {
            playlists.add(playlist);
            playlists.addAll(createSplittedPlaylists(playlist));
        }
        getRequest().setAttribute("playlists", playlists);
        forward(MyTunesRssResource.Portal);
    }

    private List<Playlist> createSplittedPlaylists(Playlist playlist) {
        List<Playlist> splittedPlaylists = new ArrayList<Playlist>();
        if (getMyTunesRssConfig().isLimitRss() && playlist.getTrackCount() > Integer.parseInt(getMyTunesRssConfig().getMaxRssItems())) {
            int maxCount = Integer.parseInt(getMyTunesRssConfig().getMaxRssItems());
            int startIndex = 0;
            while (startIndex < playlist.getTrackCount()) {
                int endIndex = Math.min(startIndex + maxCount - 1, playlist.getTrackCount() - 1);
                Playlist splittedPlaylist = new Playlist();
                splittedPlaylist.setId(playlist.getId() + "_" + startIndex + "_" + endIndex);
                splittedPlaylist.setName(playlist.getName() + " [" + createFixedLengthNumber(startIndex + 1, playlist.getTrackCount()) + "-" +
                        createFixedLengthNumber(endIndex + 1, playlist.getTrackCount()) + "]");
                splittedPlaylist.setTrackCount(endIndex - startIndex + 1);
                splittedPlaylists.add(splittedPlaylist);
                startIndex = endIndex + 1;
            }
        }
        return splittedPlaylists;
    }

    private String createFixedLengthNumber(int number, int maxNumber) {
        String fixedLengthNumber = Integer.toString(number);
        int length = Integer.toString(maxNumber).length();
        while (fixedLengthNumber.length() < length) {
            fixedLengthNumber = "0" + fixedLengthNumber;
        }
        return fixedLengthNumber;
    }
}