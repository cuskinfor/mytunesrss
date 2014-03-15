/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public class Size {
    private final int myWidth;
    private final int myHeight;

    public Size(int width, int height) {
        myWidth = width;
        myHeight = height;
    }

    public int getWidth() {
        return myWidth;
    }

    public int getHeight() {
        return myHeight;
    }

    public int getMaxSize() {
        return Math.max(myWidth, myHeight);
    }
}
