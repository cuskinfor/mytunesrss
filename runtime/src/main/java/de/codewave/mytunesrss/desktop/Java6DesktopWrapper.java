/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.desktop;

import de.codewave.mytunesrss.MyTunesRssUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

public class Java6DesktopWrapper implements DesktopWrapper {

    public Java6DesktopWrapper() {
        Desktop.getDesktop(); // just to check if it exsist
    }

    public void openBrowser(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.openBrowser", e.getMessage()));
        }
    }

    public boolean isSupported() {
        return true;
    }
}
