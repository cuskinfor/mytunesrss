/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import org.apache.commons.lang.*;

/**
 * de.codewave.mytunesrss.jsp.BundleError
 */
public class BundleError extends Error {
    public String myKey;

    public BundleError(String errorKey) {
        super((Object[])null);
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

    @Override
    public int hashCode() {
        return myKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BundleError && StringUtils.equals(myKey, ((BundleError)obj).getKey());
    }
}