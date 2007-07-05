/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import com.apple.eawt.*;
import org.apache.commons.logging.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.AppleListener
 */
public class AppleExtensions {
    private static final Log LOG = LogFactory.getLog(AppleExtensions.class);

    public static void activate(final EventListener listener) throws NoSuchMethodException {
        final Method handleQuitMethod = listener.getClass().getMethod("handleQuit");
        final Method handleReOpenApplicationMethod = listener.getClass().getMethod("handleReOpenApplication");
        Application application = Application.getApplication();
        application.setEnabledAboutMenu(false);
        application.setEnabledPreferencesMenu(false);
        application.addApplicationListener(new ApplicationAdapter() {
            @Override
            public void handleReOpenApplication(ApplicationEvent applicationEvent) {
                try {
                    handleReOpenApplicationMethod.invoke(listener);
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not invoke method for handling apple \"reOpenApplication\".", e);
                    }
                }
            }

            @Override
            public void handleQuit(ApplicationEvent applicationEvent) {
                try {
                    handleQuitMethod.invoke(listener);
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not invoke method for handling apple menu \"quit\".", e);
                    }
                }
            }
        });
    }
}