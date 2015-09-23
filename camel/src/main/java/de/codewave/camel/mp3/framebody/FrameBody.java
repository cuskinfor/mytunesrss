/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.framebody;

import de.codewave.camel.mp3.structure.Frame;

import java.util.Collection;
import java.util.Map;

/**
 * de.codewave.camel.mp3.framebody.FrameBody
 */
public abstract class FrameBody {
    private FrameBodyParser myParser;

    protected FrameBody(Frame frame) {
        if (getSupportedFrameIds() == null || getSupportedFrameIds().contains(frame.getId())) {
            myParser = new FrameBodyParser(frame.getBodyData(), frame.getBodyBeginOffset());
        } else {
            throwIllegalFrameException(frame);
        }
    }

    protected void throwIllegalFrameException(Frame frame) {
        throw new IllegalArgumentException(
                "Class \"" + getClass().getName() + "\" cannot be instantiated with a frame of type \"" + frame.getId() + "\".");
    }

    protected FrameBodyParser getParser() {
        return myParser;
    }

    protected abstract Collection<String> getSupportedFrameIds();

    protected String getJsonMap(Map<String, String> props) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> prop : props.entrySet()) {
            if (prop.getKey() != null && prop.getKey().length() > 0) {
                stringBuilder.append("\"").append(prop.getKey()).append("\":");
                if (prop.getValue() != null) {
                    stringBuilder.append("\"").append(prop.getValue()).append(
                            "\"");
                } else {
                    stringBuilder.append("null");
                }
                stringBuilder.append(",");
            }
        }
        return "{" + stringBuilder.substring(0, stringBuilder.length() - 1) + "}";
    }
}