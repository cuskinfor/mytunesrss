/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure.v4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp3.Mp3Utils;
import de.codewave.camel.mp3.exception.IllegalHeaderException;

import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.camel.mp3.structure.ExtendedHeaderV4
 */
public class ExtendedHeaderV4 {
    private Flag[] myFlags;
    private int mySize;

    ExtendedHeaderV4(InputStream stream) throws IOException, IllegalHeaderException {
        byte[] sizeData = new byte[4];
        stream.read(sizeData);
        mySize = CamelUtils.getIntValue(sizeData, 0, 4, true, Endianness.Big);
        if (mySize <= Mp3Utils.MAX_BUFFER_SIZE) {
            byte[] data = new byte[mySize - 4];
            stream.read(data);
            int flagByteCount = (int)data[0];
            myFlags = new Flag[flagByteCount * 8];
            for (int i = 0; i < flagByteCount; i++) {
                int flagByte = (int)data[1 + i];
                for (int k = 0; k < 8; k++) {
                    int bitValue = 1 << (7 - k);
                    myFlags[i * 8 + k] = new Flag((flagByte & bitValue) == bitValue);
                }
            }
            int offset = 1 + flagByteCount;
            for (Flag flag : myFlags) {
                if (flag.isSet()) {
                    int length = data[offset];
                    if (length > 0) {
                        byte[] flagData = new byte[length];
                        System.arraycopy(data, offset + 1, flagData, 0, length);
                        offset += (length + 1);
                        flag.setData(flagData);
                    }
                }
            }
        } else {
            throw new IllegalHeaderException("Maximum extended header size exceeded: " + mySize);
        }
    }

    public int getSize() {
        return mySize;
    }

    public Flag[] getFlags() {
        return myFlags.clone();
    }

    @Override
    public String toString() {
        return "ID3v2.4 Extended Header";
    }

    public static class Flag {
        private boolean mySet;
        private byte[] myData;

        public Flag(boolean set) {
            mySet = set;
        }

        public byte[] getData() {
            return myData;
        }

        public boolean isSet() {
            return mySet;
        }

        private void setData(byte[] data) {
            myData = data;
        }
    }
}