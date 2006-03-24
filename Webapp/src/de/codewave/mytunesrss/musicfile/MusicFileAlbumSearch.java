/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.musicfile;

/**
 * de.codewave.mytunesrss.musicfile.MusicFileAlbumSearch
 */
public class MusicFileAlbumSearch implements MusicFileSearch {
    private String myPattern;

    public MusicFileAlbumSearch(String pattern) {
        myPattern = pattern != null ? pattern.toLowerCase() : null;
    }

    public boolean matches(MusicFile musicFile) {
        return myPattern == null || myPattern.length() == 0 || musicFile.getAlbum().toLowerCase().contains(myPattern);
    }
}
