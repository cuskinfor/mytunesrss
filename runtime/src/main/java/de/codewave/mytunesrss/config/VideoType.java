/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

public enum VideoType {
    Movie(), TvShow(), None();

    public String toString() {
        return name();
    }
}
