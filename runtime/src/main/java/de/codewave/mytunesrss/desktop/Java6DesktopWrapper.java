/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.desktop;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class Java6DesktopWrapper implements DesktopWrapper {

    public Java6DesktopWrapper() {
        Desktop.getDesktop(); // just to check if it exsist
    }

    public void openBrowser(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            throw new RuntimeException("Could not open browser.", e);
        }
    }

    public boolean isSupported() {
        return true;
    }
}
