/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import com.apple.eawt.*;
import de.codewave.mytunesrss.settings.*;

/**
 * de.codewave.mytunesrss.AppleListener
 */
public class AppleExtensions {
    public static void activate(final Settings settings) {
        Application application = Application.getApplication();
        application.setEnabledAboutMenu(false);
        application.setEnabledPreferencesMenu(false);
        application.addApplicationListener(new ApplicationAdapter() {
            @Override
            public void handleQuit(ApplicationEvent applicationEvent) {
                settings.doQuitApplication();
            }
        });
    }
}