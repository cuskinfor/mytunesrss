/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.datastore.*;

/**
 * de.codewave.mytunesrss.datastore.statement.InsertOrUpdateTrackStatement
 */
public interface InsertOrUpdateTrackStatement extends DataStoreStatement {
    void setAlbum(String album);

    void setArtist(String artist);

    void setFileName(String fileName);

    void setId(String id);

    void setName(String name);

    void setTime(int time);

    void setTrackNumber(int trackNumber);

    void clear();
}