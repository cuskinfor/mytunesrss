/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody.v2;

import de.codewave.camel.mp3.framebody.FrameBody;
import de.codewave.camel.mp3.structure.Frame;

import java.util.Collection;

/**
 * de.codewave.camel.mp3.framebody.v2.TnnFrameBody
 */
public class TnnFrameBody extends FrameBody {
    private String myValue;

    public TnnFrameBody(Frame frame) {
        super(frame);
        if (!frame.getId().startsWith("T") || frame.getId().equals("TXX")) {
            throwIllegalFrameException(frame);
        }
        int encoding = getParser().getInteger(1);
        myValue = getParser().getMaxLengthString(encoding, frame.getBodySize() - 1);
    }

    protected Collection<String> getSupportedFrameIds() {
        return null;
    }

    public String toString() {
        return myValue;
    }
}