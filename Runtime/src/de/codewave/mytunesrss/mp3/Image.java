/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.mp3;

/**
 * de.codewave.mytunesrss.mp3.Image
 */
public class Image {
    private String myMimeType;
    private byte[] myData;

    Image(String mimeType, byte[] data) {
        myMimeType = mimeType;
        myData = data;
    }

    public String getMimeType() {
        return myMimeType;
    }

    public byte[] getData() {
        return myData;
    }
}