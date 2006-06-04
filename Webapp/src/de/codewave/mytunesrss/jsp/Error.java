/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

/**
 * de.codewave.mytunesrss.jsp.Error
 */
public abstract class Error {
    private Object[] myParameters;

    protected Error(Object... parameters) {
        myParameters = parameters;
    }

    public Object[] getParameters() {
        return myParameters;
    }

    public abstract boolean isLocalized();
}