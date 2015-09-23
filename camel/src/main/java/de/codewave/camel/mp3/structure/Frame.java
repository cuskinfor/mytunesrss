/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.structure;

import de.codewave.camel.mp3.exception.Mp3Exception;
import de.codewave.camel.mp3.structure.v2.FrameV2;
import de.codewave.camel.mp3.structure.v3.FrameV3;
import de.codewave.camel.mp3.structure.v4.FrameV4;

import java.io.IOException;

/**
 * de.codewave.camel.mp3.structure.v3.Frame
 */
public class Frame {
    private String myId;
    private int myBodySize;
    private byte[] myBodyData;

    public static Frame createFrame(int version, byte[] data, int offset) throws IOException {
        try {
            if (version == 2) {
                return new FrameV2(data, offset);
            } else if (version == 3) {
                return new FrameV3(data, offset);
            } else if (version == 4) {
                return new FrameV4(data, offset);
            }
        } catch (Mp3Exception e) {
            // intentionally left blank; just don't return a new frame
        }
        return null;
    }

    public int getBodySize() {
        return myBodySize;
    }

    public int getFrameSize() {
        return getBodySize() + 10;
    }

    public String getId() {
        return myId;
    }

    public byte[] getBodyData() {
        return myBodyData;
    }

    protected void setBodyData(byte[] bodyData) {
        myBodyData = bodyData;
    }

    protected void setBodySize(int bodySize) {
        myBodySize = bodySize;
    }

    protected void setId(String id) {
        myId = id;
    }

    public int getBodyBeginOffset() {
        return 0;
    }
}