/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * de.codewave.utils.Base64InputStream
 */
public class Base64InputStream extends InputStream {
    private Reader myReader;
    private byte[] myByteBuffer;
    private char[] myCharBuffer = new char[10240];
    private int myBufferOffset;

    public Base64InputStream(Reader reader) {
        myReader = reader;
    }

    public int read() throws IOException {
        if (myByteBuffer == null || myBufferOffset == myByteBuffer.length) {
            int readCount = myReader.read(myCharBuffer);
            if (readCount > -1) {
                myByteBuffer = Base64.decodeBase64(new String(myCharBuffer, 0, readCount).getBytes("UTF-8"));
            } else {
                myByteBuffer = null;
            }
            myBufferOffset = 0;
        }
        if (myByteBuffer != null && myByteBuffer.length > 0 && myBufferOffset < myByteBuffer.length) {
            return (int)myByteBuffer[myBufferOffset++];
        }
        return -1;
    }
}