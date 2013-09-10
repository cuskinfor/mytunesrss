/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public class MessageWithParameters {

    private String myMessage;
    private Object[] myParameters;

    public MessageWithParameters(String message, Object... parameters) {
        myMessage = message;
        myParameters = parameters;
    }

    public String getMessage() {
        return myMessage;
    }

    public Object[] getParameters() {
        return myParameters;
    }
}
