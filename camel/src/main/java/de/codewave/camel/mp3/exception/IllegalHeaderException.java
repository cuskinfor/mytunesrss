/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.exception;

/**
 * de.codewave.camel.mp3.exception.IllegalHeaderException
 */
public class IllegalHeaderException extends Mp3Exception {
    public IllegalHeaderException() {
        super();
    }

    public IllegalHeaderException(String string) {
        super(string);
    }

    public IllegalHeaderException(String string, Throwable throwable) {
        super(string, throwable);
    }

    public IllegalHeaderException(Throwable throwable) {
        super(throwable);
    }
}