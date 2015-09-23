/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody.v3;

import de.codewave.camel.mp3.framebody.FrameBody;
import de.codewave.camel.mp3.structure.Frame;

import java.util.Collection;
import java.util.Collections;

/**
 * de.codewave.camel.mp3.framebody.v3.COMMFrameBody
 */
public class COMMFrameBody extends FrameBody {
    private String myLanguage;
    private String myComment;
    private String myDescription;

    public COMMFrameBody(Frame frame) {
        super(frame);
        int encoding = getParser().getInteger(1);
        myLanguage = getParser().getFixedLengthString(3);
        myDescription = getParser().getString(encoding);
        myComment = getParser().getMaxLengthString(encoding, frame.getBodySize() - myDescription.length() - 4);
    }

    protected Collection<String> getSupportedFrameIds() {
        return Collections.singleton("COMM");
    }

    public String getComment() {
        return myComment;
    }

    public String getDescription() {
        return myDescription;
    }

    public String getLanguage() {
        return myLanguage;
    }

    public String toString() {
        return getComment();
    }
}