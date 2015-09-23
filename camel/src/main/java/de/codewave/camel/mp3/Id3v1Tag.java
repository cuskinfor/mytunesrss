/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;

import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.camel.mp3.Id3v1Tag
 */
public class Id3v1Tag implements Id3Tag {
    public static Id3v1Tag createTag(InputStream stream) {
        try {
            byte[] tagBuffer = new byte[128];
            byte[] readBuffer = new byte[128];
            for (int count = stream.read(readBuffer); count > -1; count = stream.read(readBuffer)) {
                if (count > 0) {
                    System.arraycopy(tagBuffer, count, tagBuffer, 0, 128 - count);
                    System.arraycopy(readBuffer, 0, tagBuffer, 128 - count, count);
                }
            }
            if (CamelUtils.getIntValue(tagBuffer, 0, 3, false, Endianness.Big) == ('T' << 16 | 'A' << 8 | 'G')) {
                return new Id3v1Tag(tagBuffer);
            }
        } catch (IOException e) {
            // intentionally left blank
        }
        return null;
    }

    private String myTitle;
    private String myArtist;
    private String myAlbum;
    private String myYear;
    private String myComment;
    private int myTrackNumber;
    private int myGenre;
    private boolean myVersion11;

    public Id3v1Tag(byte[] data) {
        myTitle = CamelUtils.getString(data, 3, 30, CamelUtils.DEFAULT_CHARSET).trim();
        myArtist = CamelUtils.getString(data, 33, 30, CamelUtils.DEFAULT_CHARSET).trim();
        myAlbum = CamelUtils.getString(data, 63, 30, CamelUtils.DEFAULT_CHARSET).trim();
        myYear = CamelUtils.getString(data, 93, 4, CamelUtils.DEFAULT_CHARSET).trim();
        myGenre = CamelUtils.getIntValue(data, 127, 1, false, Endianness.Big);
        if (CamelUtils.getIntValue(data, 126, 1, false, Endianness.Big) == 0x00) {
            myVersion11 = true;
            myTrackNumber = CamelUtils.getIntValue(data, 126, 1, false, Endianness.Big);
            myComment = CamelUtils.getString(data, 97, 28, CamelUtils.DEFAULT_CHARSET).trim();
        } else {
            myComment = CamelUtils.getString(data, 97, 30, CamelUtils.DEFAULT_CHARSET).trim();
        }
    }

    public String getAlbum() {
        return myAlbum;
    }

    public String getArtist() {
        return myArtist;
    }

    public String getComment() {
        return myComment;
    }

    public int getGenre() {
        return myGenre;
    }

    public String getGenreAsString() {
        return Mp3Utils.translateGenre(Integer.toString(getGenre())).trim();
    }

    public String getTitle() {
        return myTitle;
    }

    public String getYear() {
        return myYear;
    }

    public String getShortVersionIdentifier() {
        return getLongVersionIdentifier();
    }

    public String getLongVersionIdentifier() {
        return myVersion11 ? "ID3v1.1" : "ID3v1.0";
    }

    public boolean isId3v1() {
        return true;
    }

    public boolean isId3v2() {
        return false;
    }

    public int getTrackNumber() {
        return myTrackNumber;
    }

    @Override
    public String toString() {
        String info = getShortVersionIdentifier() + " Tag: album=" + getAlbum() + ", artist=" + getArtist() + ", title=" + getTitle() + ", year=" +
                getYear() + ", comment=" + getComment() + ", genre=" + getGenre();
        return myVersion11 ? info + ", track=" + getTrackNumber() : info;
    }
}
