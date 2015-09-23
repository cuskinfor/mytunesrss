/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody.v3;

import de.codewave.camel.mp3.framebody.FrameBody;
import de.codewave.camel.mp3.structure.Frame;

import java.util.Collection;
import java.util.Collections;

/**
 * de.codewave.camel.mp3.framebody.v3.WXXXFrameBody
 */
public class WXXXFrameBody extends FrameBody {
    private String myUrl;
    private String myDescription;

    public WXXXFrameBody(Frame frame) {
        super(frame);
        int encoding = getParser().getInteger(1);
        myDescription = getParser().getString(encoding);
        myUrl = getParser().getMaxLengthString(encoding, frame.getBodySize() - myDescription.length() - 2);
    }

    protected Collection<String> getSupportedFrameIds() {
        return Collections.singleton("WXXX");
    }

    public String getUrl() {
        return myUrl;
    }

    public String getDescription() {
        return myDescription;
    }

    public String toString() {
        return getUrl();
    }
}