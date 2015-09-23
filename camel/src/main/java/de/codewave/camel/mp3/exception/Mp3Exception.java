/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3.exception;

/**
 * de.codewave.camel.mp3.Mp3Exception
 */
public class Mp3Exception extends Exception {
    public Mp3Exception() {
        super();
    }

    public Mp3Exception(String string) {
        super(string);
    }

    public Mp3Exception(String string, Throwable throwable) {
        super(string, throwable);
    }

    public Mp3Exception(Throwable throwable) {
        super(throwable);
    }
}