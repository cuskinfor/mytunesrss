/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

/**
 * de.codewave.mytunesrss.jsp.PagerItem
 */
public class PagerItem {
    private String myKey;
    private String myValue;

    public PagerItem(String key, String value) {
        myKey = key;
        myValue = value;
    }

    public String getKey() {
        return myKey;
    }

    public String getValue() {
        return myValue;
    }
}