/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertITunesPlaylistStatement
 */
public class InsertITunesPlaylistStatement extends InsertPlaylistStatement {

    public InsertITunesPlaylistStatement() {
        setType(PlaylistType.ITunes);
    }
}