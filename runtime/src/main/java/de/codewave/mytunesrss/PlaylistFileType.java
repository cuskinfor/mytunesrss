/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public enum PlaylistFileType {
    Xspf("XSPF"), M3u("M3U"), Json("JSON");

    private String myText;

    PlaylistFileType(String text) {
        myText = text;
    }

    @Override
    public String toString() {
        return myText;
    }
}
