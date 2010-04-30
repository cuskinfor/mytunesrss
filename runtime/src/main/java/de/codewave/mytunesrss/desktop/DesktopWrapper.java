/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.desktop;

import java.net.URI;

public interface DesktopWrapper {
    void openBrowser(URI uri);

    boolean isSupported();
}
