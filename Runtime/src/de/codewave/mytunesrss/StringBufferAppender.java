/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.StringBufferAppender
 */
public class StringBufferAppender extends AppenderSkeleton {
    List<String> myBuffer = new ArrayList<String>();
    int myMaximumBufferSize = 2000;

    protected synchronized void append(LoggingEvent loggingEvent) {
        myBuffer.add(loggingEvent.getRenderedMessage() + System.getProperty("line.separator"));
        while (myBuffer.size() > myMaximumBufferSize) {
            myBuffer.remove(0);
        }
    }

    public void close() {
        myBuffer = null;
    }

    public boolean requiresLayout() {
        return false;
    }

    public String getText() {
        StringBuffer text = new StringBuffer();
        for (String line : myBuffer) {
            text.append(line);
        }
        return text.toString();
    }
}