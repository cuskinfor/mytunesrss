/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody.v3;

import de.codewave.camel.mp3.framebody.FrameBody;
import de.codewave.camel.mp3.framebody.FrameBodyParser;
import de.codewave.camel.mp3.structure.Frame;

import java.util.Collection;

/**
 * de.codewave.camel.mp3.framebody.v3.WnnnFrameBody
 */
public class WnnnFrameBody extends FrameBody {
    private String myUrl;

    public WnnnFrameBody(Frame frame) {
        super(frame);
        if (!frame.getId().startsWith("W") || frame.getId().equals("WXXX")) {
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