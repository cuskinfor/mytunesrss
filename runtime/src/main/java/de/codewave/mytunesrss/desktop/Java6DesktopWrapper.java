/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.desktop;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Java6DesktopWrapper implements DesktopWrapper {

    public void openHomepage() {
        try {
            Desktop.getDesktop().browse(new URI("http://www.codewave.de"));
        } catch (IOException e) {
            throw new RuntimeException("Could not open homepage.", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not open homepage.", e);
        }
    }

    public void openDocumentation() {
        try {
            Desktop.getDesktop().browse(new URI("http://docs.codewave.de/mytunesrss"));
        } catch (IOException e) {
            throw new RuntimeException("Could not open homepage.", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not open homepage.", e);
        }

    }

    public void mailSupport() {
        try {
            Desktop.getDesktop().mail(new URI("mailto:support@codewave.de"));
        } catch (IOException e) {
            throw new RuntimeException("Could not open homepage.", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not open homepage.", e);
        }
    }

    public boolean isSupported() {
        return true;
    }
}
