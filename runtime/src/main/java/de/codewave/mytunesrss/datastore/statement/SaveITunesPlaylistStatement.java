/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveITunesPlaylistStatement
 */
public class SaveITunesPlaylistStatement extends SavePlaylistStatement {

    public SaveITunesPlaylistStatement(String sourceId, boolean folder) {
        super(sourceId);
        setType(folder ? PlaylistType.ITunesFolder : PlaylistType.ITunes);
    }

}
