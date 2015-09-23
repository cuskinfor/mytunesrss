package de.codewave.systray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * de.codewave.systray.SystrayUtil
 */
class SystrayInternalUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystrayInternalUtils.class);

    private static Map<UUID, TrayIcon> myTrayIcons = new HashMap<UUID, TrayIcon>();

    private SystrayInternalUtils() {
        // Private utility class constructor.
    }

    static UUID add(Image icon, String tooltip, PopupMenu popupMenu, ActionListener actionListener) {
        UUID id = null;
        if (SystemTray.isSupported()) {
            id = UUID.randomUUID();
            TrayIcon trayIcon = new TrayIcon(icon, tooltip, popupMenu);
            trayIcon.setImageAutoSize(true);
            if (actionListener != null) {
                trayIcon.addActionListener(actionListener);
            }
            try {
                SystemTray.getSystemTray().add(trayIcon);
                myTrayIcons.put(id, trayIcon);
            } catch (AWTException e) {
                LOGGER.error("Could not add system tray icon.", e);
                id = null;
            }
        }
        return id;
    }

    static void remove(UUID id) {
        TrayIcon trayIcon = myTrayIcons.get(id);
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            myTrayIcons.remove(id);
        }
    }

}