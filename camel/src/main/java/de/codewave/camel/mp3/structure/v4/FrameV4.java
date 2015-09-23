/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure.v4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import de.codewave.camel.mp3.exception.Mp3Exception;
import de.codewave.camel.mp3.structure.v3.FrameV3;

import java.io.IOException;

/**
 * de.codewave.camel.mp3.structure.FrameV4
 */
public class FrameV4 extends FrameV3 {
    private static final int FLAG_DATA_LENGTH_INDICATOR = 1;
    private static final int FLAG_DATA_UNSYNCHRONISATION = 2;

    public FrameV4(byte[] data, int offset) throws IOException, Mp3Exception {
        super(data, offset);
    }

    protected void setBodySize(byte[] data, int offset) {
        if (isDataLengthIndicator()) {
            setBodySize(CamelUtils.getIntValue(data, offset + 10, 4, true, Endianness.Big));
        } else {
            setBodySize(CamelUtils.getIntValue(data, offset + 4, 4, true, Endianness.Big));
        }
    }

    @Override
    public String toString() {
        return "ID3v2.4 Frame: " + getId() + ", flags=0x" + Integer.toHexString(getFlags()) + ", bodySize=" + getBodySize();
    }

    public boolean isDataLengthIndicator() {
        return (getFlags() & FLAG_DATA_LENGTH_INDICATOR) == FLAG_DATA_LENGTH_INDICATOR;
    }

    @Override
    public int getBodyBeginOffset() {
        return isDataLengthIndicator() ? 4 : 0;
    }

    @Override
    protected int getFrameHeaderSize() {
        return super.getFrameHeaderSize() + getBodyBeginOffset();
    }

    @Override
    protected boolean isUnsynchronisation() {
        return (getFlags() & FLAG_DATA_UNSYNCHRONISATION) == FLAG_DATA_UNSYNCHRONISATION;
    }
}