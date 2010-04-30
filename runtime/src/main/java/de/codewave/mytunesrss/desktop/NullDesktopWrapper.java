/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.desktop;

import java.net.URI;

public class NullDesktopWrapper implements DesktopWrapper {
    public void openBrowser(URI uri) {
        // intentionally left blank
    }

    public boolean isSupported() {
        return false;
    }
}
