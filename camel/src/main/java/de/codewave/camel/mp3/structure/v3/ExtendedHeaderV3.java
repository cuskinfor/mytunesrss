/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure.v3;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp3.exception.IllegalHeaderException;

import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.camel.mp3.structure.ExtendedHeaderV3
 */
public class ExtendedHeaderV3 {
    private int myFlags;
    private int myCrcValue;
    private int myPaddingSize;
    private int mySize;

    ExtendedHeaderV3(InputStream stream) throws IOException, IllegalHeaderException {
        byte[] sizeData = new byte[4];
        stream.read(sizeData);
        mySize = CamelUtils.getIntValue(sizeData, 0, 4, false, Endianness.Big) + 4;
        if (mySize <= Mp3Utils.MAX_BUFFER_SIZE) {
            byte[] data = new byte[mySize - 4];
            stream.read(data);
            myFlags = CamelUtils.getIntValue(data, 0, 2, false, Endianness.Big);
            myPaddingSize = CamelUtils.getIntValue(data, 2, 4, false, Endianness.Big);
            if (isCrc()) {
                myCrcValue = CamelUtils.getIntValue(data, 6, 4, false, Endianness.Big);
            }
        } else {
            throw new IllegalHeaderException("Maximum extended header size exceeded: "+ mySize);
        }
    }

    public boolean isCrc() {
        return (myFlags & 0x8000) == 0x8000;
    }

    public int getCrcValue() {
        return myCrcValue;
    }

    public int getPaddingSize() {
        return myPaddingSize;
    }

    public int getSize() {
        return mySize;
    }

    @Override
    public String toString() {
        if (isCrc()) {
            return "ID3v2.3 Extended Header: crc=" + getCrcValue() + ", padding=" + getPaddingSize();
        } else {
            return "ID3v2.3 Extended Header: padding=" + getPaddingSize();
        }
    }
}