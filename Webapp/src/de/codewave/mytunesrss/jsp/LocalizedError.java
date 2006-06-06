/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

/**
 * de.codewave.mytunesrss.jsp.LocalizedError
 */
public class LocalizedError extends Error {
    private String myMessage;

    public LocalizedError(String message) {
        super((Object[])null);
        myMessage = message;
    }

    public LocalizedError(String message, Object... parameters) {
        super(parameters);
        myMessage = message;
    }

    public String getMessage() {
        return myMessage;
    }

    public boolean isLocalized() {
        return true;
    }
}