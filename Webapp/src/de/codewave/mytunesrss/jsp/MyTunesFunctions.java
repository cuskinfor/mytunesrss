/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.datastore.statement.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.jsp.MyTunesFunctions
 */
public class MyTunesFunctions {
    private static final String DEFAULT_NAME = "MyTunesRSS";

    public static String virtualName(File file) {
        String name = file.getName();
        name = createCleanFileName(name);
        return name;
    }

    private static String createCleanFileName(String name) {
        return name.replace(' ', '_');
    }

    public static boolean unknown(String trackAlbumOrArtist) {
        return InsertTrackStatement.UNKNOWN.equals(trackAlbumOrArtist);
    }

    public static String virtualAlbumName(Album album) {
        if (unknown(album.getArtist()) && unknown(album.getName())) {
            return DEFAULT_NAME;
        } else if (unknown(album.getArtist())) {
            return createCleanFileName(album.getName());
        }
        return createCleanFileName(album.getArtist() + " - " + album.getName());
    }
}