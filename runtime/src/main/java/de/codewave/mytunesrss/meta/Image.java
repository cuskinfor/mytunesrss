/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.meta;

/**
 * de.codewave.mytunesrss.meta.Image
 */
public class Image {
    private String myMimeType;
    private byte[] myData;

    public Image(String mimeType, byte[] data) {
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