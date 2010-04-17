/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.meta;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.mytunesrss.meta.Image
 */
public class Image {
    private String myMimeType;
    private byte[] myData;

    public Image(String mimeType, InputStream is) throws IOException {
        this(mimeType, IOUtils.toByteArray(new BufferedInputStream(is)));
    }

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