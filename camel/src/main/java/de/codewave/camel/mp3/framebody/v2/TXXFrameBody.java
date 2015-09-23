/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody.v2;

import de.codewave.camel.mp3.framebody.FrameBody;
import de.codewave.camel.mp3.structure.Frame;

import java.util.Collection;
import java.util.Collections;

/**
 * de.codewave.camel.mp3.framebody.v2.TXXFrameBody
 */
public class TXXFrameBody extends FrameBody {
    private String myValue;
    private String myDescription;

    public TXXFrameBody(Frame frame) {
        super(frame);
        int encoding = getParser().getInteger(1);
        myDescription = getParser().getString(encoding);
        myValue = getParser().getMaxLengthString(encoding, frame.getBodySize() - myDescription.length() - 2);
    }

    protected Collection<String> getSupportedFrameIds() {
        return Collections.singleton("TXX");
    }

    public String getValue() {
        return myValue;
    }

    public String getDescription() {
        return myDescription;
    }

    public String toString() {
        return getValue();
    }
}