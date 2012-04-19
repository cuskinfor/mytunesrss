/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.vlc;

public class VlcPlayerException extends Exception {
    public VlcPlayerException(String message) {
        super(message);
    }

    public VlcPlayerException(String message, Throwable cause) {
        super(message, cause);
    }

    public VlcPlayerException(Throwable cause) {
        super(cause);
    }
}