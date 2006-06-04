/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

/**
 * de.codewave.mytunesrss.jsp.BundleError
 */
public class BundleError extends Error {
    public String myKey;

    public BundleError(String errorKey) {
        super(null);
        myKey = errorKey;
    }

    public BundleError(String errorKey, Object... parameters) {
        super(parameters);
        myKey = errorKey;
    }

    public String getKey() {
        return myKey;
    }

    public boolean isLocalized() {
        return false;
    }
}