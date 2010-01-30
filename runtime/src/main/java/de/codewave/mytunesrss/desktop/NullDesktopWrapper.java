/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.desktop;

public class NullDesktopWrapper implements DesktopWrapper {
    public void openHomepage() {
        // intentionally left blank
    }

    public void openDocumentation() {
        // intentionally left blank
    }

    public void mailSupport() {
        // intentionally left blank
    }

    public boolean isSupported() {
        return false;
    }
}
