/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.rss4psp.musicfile;

/**
 * de.codewave.rss4psp.musicfile.MusicFileAlbumSearch
 */
public class MusicFileArtistSearch implements MusicFileSearch {
    private String myPattern;

    public MusicFileArtistSearch(String pattern) {
        myPattern = pattern != null ? pattern.toLowerCase() : null;
    }

    public boolean matches(MusicFile musicFile) {
        return myPattern == null || myPattern.length() == 0 || musicFile.getArtist().toLowerCase().contains(myPattern);
    }
}