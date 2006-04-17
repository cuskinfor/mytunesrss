/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.musicfile;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.musicfile.MusicFileAlbumSearch
 */
public class MusicFileArtistSearch implements MusicFileSearch {
    private String myPattern;

    public MusicFileArtistSearch(String pattern) {
        myPattern = pattern != null ? pattern.toLowerCase() : null;
    }

    public boolean matches(MusicFile musicFile) {
        return StringUtils.isEmpty(myPattern) || musicFile.getArtist().toLowerCase().contains(myPattern);
    }
}