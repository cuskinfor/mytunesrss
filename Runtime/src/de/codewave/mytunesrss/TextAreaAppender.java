/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.log4j.*;
import org.apache.log4j.spi.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.TextAreaAppender
 */
public class TextAreaAppender extends AppenderSkeleton {
    JTextArea myTextArea;


    public TextAreaAppender(JTextArea textArea) {
        myTextArea = textArea;
    }

    protected void append(LoggingEvent loggingEvent) {
        myTextArea.append(loggingEvent.getRenderedMessage() + System.getProperty("line.separator"));
    }

    public void close() {
        // intentionally left blank
    }

    public boolean requiresLayout() {
        return false;
    }
}