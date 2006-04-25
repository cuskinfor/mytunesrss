/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import com.apple.eawt.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.AppleExtensions
 */
public class AppleExtensions {
    public static void activate(final JFrame frame, final Settings settings) {
        Application application = Application.getApplication();
        application.removeAboutMenuItem();
        application.removePreferencesMenuItem();
        application.addApplicationListener(new ApplicationListener() {
            public void handleAbout(ApplicationEvent applicationEvent) {
                applicationEvent.setHandled(true);
            }

            public void handleOpenApplication(ApplicationEvent applicationEvent) {
                applicationEvent.setHandled(true);
            }

            public void handleOpenFile(ApplicationEvent applicationEvent) {
                applicationEvent.setHandled(true);
            }

            public void handlePreferences(ApplicationEvent applicationEvent) {
                applicationEvent.setHandled(true);
            }

            public void handlePrintFile(ApplicationEvent applicationEvent) {
                applicationEvent.setHandled(true);
            }

            public void handleQuit(ApplicationEvent applicationEvent) {
                applicationEvent.setHandled(true);
                settings.doQuitApplication();
            }

            public void handleReOpenApplication(ApplicationEvent applicationEvent) {
                applicationEvent.setHandled(true);
            }
        });
    }
}