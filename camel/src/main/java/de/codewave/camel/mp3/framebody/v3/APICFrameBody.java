/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody.v3;

import de.codewave.camel.mp3.framebody.FrameBody;
import de.codewave.camel.mp3.structure.Frame;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * de.codewave.camel.mp3.framebody.v3.APICFrameBodyy
 */
public class APICFrameBody extends FrameBody {
    private String myMimeType;
    private int myPictureType;
    private String myDescription;
    private byte[] myPictureData;

    public APICFrameBody(Frame frame) {
        super(frame);
        int encoding = getParser().getInteger(1);
        myMimeType = getParser().getString();
        myPictureType = getParser().getInteger(1);
        myDescription = getParser().getString(encoding);
        myPictureData = getParser().getBytes();
    }

    protected Collection<String> getSupportedFrameIds() {
        return Collections.singleton("APIC");
    }

    public String getDescription() {
        return myDescription;
    }

    public String getMimeType() {
        return myMimeType;
    }

    public int getPictureType() {
        return myPictureType;
    }

    public byte[] getPictureData() {
        return myPictureData;
    }

    public String toString() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("description", getDescription());
        props.put("mimetype", getMimeType());
        props.put("picturetype", Integer.toString(getPictureType()));
        props.put("datalength", Integer.toString(getPictureData().length));
        return getJsonMap(props);
    }
}