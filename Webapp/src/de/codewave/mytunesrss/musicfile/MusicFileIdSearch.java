/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.musicfile;

/**
 * de.codewave.mytunesrss.musicfile.MusicFileIdSearch
 */
public class MusicFileIdSearch implements MusicFileSearch {
    private String myId;


    public MusicFileIdSearch(String id) {
        myId = id;
    }

    public boolean matches(MusicFile musicFile) {
        return musicFile.getId().equals(myId);
    }
}