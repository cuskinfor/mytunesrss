/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

/**
 * de.codewave.camel.mp3.Id3Tag
 */
public interface Id3Tag {
    String getShortVersionIdentifier();
    String getLongVersionIdentifier();
    boolean isId3v1();
    boolean isId3v2();
    String getTitle();
    String getAlbum();
    String getArtist();
    String getGenreAsString();
    String getYear();
}