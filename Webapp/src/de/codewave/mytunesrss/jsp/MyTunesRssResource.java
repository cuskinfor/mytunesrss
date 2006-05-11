/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

public enum MyTunesRssResource {
    Login,Portal;

    public String getValue() {
        switch (this) {
            case Login:
                return "/login.jsp";
            case Portal:
                return "/portal.jsp";
            default:
                throw new IllegalArgumentException("Illegal resource!");
        }
    }
}
