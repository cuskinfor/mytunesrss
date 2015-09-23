package de.codewave.systray;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.UUID;

/**
 * de.codewave.systray.SystrayFactory
 */
public class SystrayUtils {

    private SystrayUtils() {
        // Private utility class constructor.
    }

    public static UUID add(Image icon, String tooltip, PopupMenu popupMenu, ActionListener actionListener) {
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_6)) {
            return SystrayInternalUtils.add(icon, tooltip, popupMenu, actionListener);
        }
        return null;
    }

    public static void remove(UUID handle) {
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_6)) {
            SystrayInternalUtils.remove(handle);
        }
    }

}
