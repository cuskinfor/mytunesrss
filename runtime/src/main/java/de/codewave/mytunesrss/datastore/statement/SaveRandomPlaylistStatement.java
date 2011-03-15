/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

/**
 * de.codewave.mytunesrss.datastore.statement.SaveTempPlaylistStatement
 */
public class SaveRandomPlaylistStatement extends SavePlaylistStatement {

    public SaveRandomPlaylistStatement() {
        setType(PlaylistType.Random);
    }

}