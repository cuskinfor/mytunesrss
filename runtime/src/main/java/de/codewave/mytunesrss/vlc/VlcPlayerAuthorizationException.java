/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.vlc;

public class VlcPlayerAuthorizationException extends VlcPlayerException {
    public VlcPlayerAuthorizationException(String message) {
        super(message);
    }

    public VlcPlayerAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public VlcPlayerAuthorizationException(Throwable cause) {
        super(cause);
    }

    @Override
    public boolean isPermanent() {
        return true;
    }
}
