/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.utils.*;

import javax.servlet.http.*;
import java.net.*;

/**
 * de.codewave.mytunesrss.jsp.MyTunesFunctions
 */
public class MyTunesFunctions {
    private static final String DEFAULT_NAME = "MyTunesRSS";

    public static String cleanFileName(String name) {
        name = name.replace('/', '_');
        return name.replace(' ', '_');
    }

    public static boolean unknown(String trackAlbumOrArtist) {
        return InsertTrackStatement.UNKNOWN.equals(trackAlbumOrArtist);
    }

    public static String virtualTrackName(Track track) {
        if (unknown(track.getArtist())) {
            return cleanFileName(track.getName());
        }
        return cleanFileName(track.getArtist() + " - " + track.getName());
    }


    public static String virtualAlbumName(Album album) {
        if (unknown(album.getArtist()) && unknown(album.getName())) {
            return DEFAULT_NAME;
        } else if (unknown(album.getArtist())) {
            return cleanFileName(album.getName());
        } else if (album.getArtistCount() > 1) {
            return cleanFileName(album.getName());
        }
        return cleanFileName(album.getArtist() + " - " + album.getName());
    }

    public static String virtualArtistName(Artist artist) {
        if (unknown(artist.getName())) {
            return DEFAULT_NAME;
        }
        return cleanFileName(artist.getName());
    }

    public static String virtualSuffix(WebConfig webConfig, Track track) {
        return webConfig.getSuffix(track.getFile());
    }

    public static String replace(String string, String target, String replacement) {
        return string.replace(target, replacement);
    }

    public static String hexCode(String string) {
        return MiscUtils.toHexString(string);
    }
}