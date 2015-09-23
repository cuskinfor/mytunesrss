/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure.v3;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp3.exception.Mp3Exception;
import de.codewave.camel.mp3.structure.Frame;

import java.io.IOException;

/**
 * de.codewave.camel.mp3.structure.FrameV3
 */
public class FrameV3 extends Frame {
    private static final int DEFAULT_FRAME_HEADER_SIZE = 10;

    private int myFlags;

    public FrameV3(byte[] data, int offset) throws IOException, Mp3Exception {
        if (data.length >= offset + DEFAULT_FRAME_HEADER_SIZE) {
            setId(new String(new char[] {(char)data[offset], (char)data[offset + 1], (char)data[offset + 2], (char)data[offset + 3]}));
            setBodySize(data, offset);
            myFlags = CamelUtils.getIntValue(data, offset + 8, 2, false, Endianness.Big);
            if (getBodySize() >= 0 && data.length >= offset + getFrameHeaderSize() + getBodySize()) {
                setBodyData(new byte[getBodySize()]);
                if (getBodySize() > 0) {
                    if (isUnsynchronisation()) {
                        int dest = 1;
                        byte lastByte = data[offset + getFrameHeaderSize()];
                        getBodyData()[0] = lastByte;
                        for (int i = 1; i < getBodySize(); i++) {
                            byte currentByte = data[offset + getFrameHeaderSize() + i];
                            if (lastByte != (byte)0xFF || currentByte != (byte)0x00) {
                                getBodyData()[dest++] = currentByte;
                            }
                            lastByte = currentByte;
                        }
                    } else {
                        System.arraycopy(data, offset + getFrameHeaderSize(), getBodyData(), 0, getBodySize());
                    }
                }
            } else {
                throw new Mp3Exception("Invalid frame data.");
            }
        } else {
            throw new Mp3Exception("Invalid frame data.");
        }
    }

    protected boolean isUnsynchronisation() {
        return false;
    }

    protected int getFrameHeaderSize() {
        return DEFAULT_FRAME_HEADER_SIZE;
    }

    protected void setBodySize(byte[] data, int offset) {
        setBodySize(CamelUtils.getIntValue(data, offset + 4, 4, false, Endianness.Big));
    }

    public int getFrameSize() {
        return getBodySize() + getFrameHeaderSize();
    }

    public int getFlags() {
        return myFlags;
    }

    @Override
    public String toString() {
        return "ID3v2.3 Frame: " + getId() + ", flags=0x" + Integer.toHexString(getFlags()) + ", bodySize=" + getBodySize();
    }
}

