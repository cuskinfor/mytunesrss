/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.desktop;

/**
 * Factory for desktop wrapper.
 */
public class DesktopWrapperFactory {

    /**
     * Create a desktop wrapper.
     *
     * @return A desktop wrapper.
     */
    public static final DesktopWrapper createDesktopWrapper() {
        try {
            Class.forName("java.awt.Desktop");
            // class found, so we have JDK 1.6 or better and we can use the Java 6 wrapper
            return new Java6DesktopWrapper();
        } catch (ClassNotFoundException e) {
            // class not found, so we use a null wrapper which does nothing
            return new NullDesktopWrapper();
        }
    }
}
