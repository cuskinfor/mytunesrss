/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public enum VideoType {
    Movie(), TvShow();

    public String toString() {
        return name();
    }
}
