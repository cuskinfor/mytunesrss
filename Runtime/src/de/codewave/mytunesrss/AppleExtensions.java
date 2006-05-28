/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import com.apple.eawt.*;

import java.awt.event.*;

/**
 * de.codewave.mytunesrss.AppleListener
 */
public class AppleExtensions {
    public static void activate(final WindowListener windowListener) {
        Application application = Application.getApplication();
        application.setEnabledAboutMenu(false);
        application.setEnabledPreferencesMenu(false);
        application.addApplicationListener(new ApplicationAdapter() {
            @Override
            public void handleQuit(ApplicationEvent applicationEvent) {
                windowListener.windowClosing(null);
            }
        });
    }
}