/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.itunes;

import de.codewave.mytunesrss.musicfile.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.itunes.PlayList
 */
public class PlayList {
    private List<MusicFile> myMusicFiles = new ArrayList<MusicFile>();
    private String myName;
    private String myId;


    public PlayList(String id, String name) {
        myId = id;
        myName = name;
    }

    public void addMusicFile(MusicFile musicFile) {
        myMusicFiles.add(musicFile);
    }

    public List<MusicFile> getMusicFiles() {
        return Collections.unmodifiableList(myMusicFiles);
    }

    public String getId() {
        return myId;
    }

    public String getName() {
        return myName;
    }
}