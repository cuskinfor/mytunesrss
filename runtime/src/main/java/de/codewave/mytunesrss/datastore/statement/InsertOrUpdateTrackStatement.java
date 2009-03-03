/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.mytunesrss.MediaType;

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

    void setProtected(boolean aProtected);

    void setMediaType(MediaType mediaType);

    void setGenre(String genre);

    void setMp4Codec(String codec);

    void setComment(String comment);

    void setPos(int number, int size);

    void clear();
}