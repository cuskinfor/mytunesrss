/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody.v2;

import de.codewave.camel.mp3.framebody.FrameBody;
import de.codewave.camel.mp3.framebody.FrameBodyParser;
import de.codewave.camel.mp3.structure.Frame;

import java.util.Collection;

/**
 * de.codewave.camel.mp3.framebody.v2.WnnFrameBody
 */
public class WnnFrameBody extends FrameBody {
    private String myUrl;

    public WnnFrameBody(Frame frame) {
        super(frame);
        if (!frame.getId().startsWith("W") || frame.getId().equals("WXX")) {
            throwIllegalFrameException(frame);
        }
        myUrl = getParser().getMaxLengthString(FrameBodyParser.ENCODING_ISO_8859_1, frame.getBodySize());
    }

    protected Collection<String> getSupportedFrameIds() {
        return null;
    }

    public String getUrl() {
        return myUrl;
    }

    public String toString() {
        return getUrl();
    }
}