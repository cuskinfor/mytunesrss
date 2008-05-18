/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveTempPlaylistStatement
 */
public class SaveTempPlaylistStatement extends SavePlaylistStatement {

    public SaveTempPlaylistStatement() {
        setType(PlaylistType.TEMP);
    }

}