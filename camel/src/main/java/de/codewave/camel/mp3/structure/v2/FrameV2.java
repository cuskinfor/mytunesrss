/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure.v2;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp3.exception.Mp3Exception;
import de.codewave.camel.mp3.structure.Frame;

import java.io.IOException;

/**
 * de.codewave.camel.mp3.structure.FrameV2
 */
public class FrameV2 extends Frame {
    private static final int FRAME_SIZE = 6;

    public FrameV2(byte[] data, int offset) throws IOException, Mp3Exception {
        if (data.length >= offset + FRAME_SIZE) {
            setId(new String(new char[] {(char)data[offset], (char)data[offset + 1], (char)data[offset + 2]}));
            setBodySize(CamelUtils.getIntValue(data, offset + 3, 3, false, Endianness.Big));
            if (getBodySize() >= 0 && data.length >= offset + FRAME_SIZE + getBodySize()) {
                setBodyData(new byte[getBodySize()]);
                if (getBodySize() > 0) {
                    System.arraycopy(data, offset + FRAME_SIZE, getBodyData(), 0, getBodySize());
                }
            } else {
                throw new Mp3Exception("Invalid frame data.");
            }
        } else {
            throw new Mp3Exception("Invalid frame data.");
        }
    }

    public int getFrameSize() {
        return getBodySize() + FRAME_SIZE;
    }


    @Override
    public String toString() {
        return "ID3v2.2 Frame: " + getId() + ", bodySize=" + getBodySize();
    }
}