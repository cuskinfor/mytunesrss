/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

/**
 * de.codewave.mytunesrss.jsp.Error
 */
public abstract class Error {
    private Object[] myParameters;
    private boolean escapeXml = true;

    protected Error(Object... parameters) {
        myParameters = parameters;
    }

    public Object[] getParameters() {
        return myParameters;
    }

    public boolean isEscapeXml() {
        return escapeXml;
    }

    public void setEscapeXml(boolean escapeXml) {
        this.escapeXml = escapeXml;
    }

    public abstract boolean isLocalized();
}