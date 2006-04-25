/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.moduleinfo.*;
import de.codewave.utils.serialnumber.*;
import org.apache.catalina.*;
import org.apache.commons.logging.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    private static final Log LOG = LogFactory.getLog(MyTunesRss.class);
    public static boolean REGISTERED;

    public static void main(String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException,
            ClassNotFoundException, IOException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ResourceBundle mainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        String version = modulesInfo.getVersion();
        System.setProperty("mytunesrss.version", version);
        JFrame frame = new JFrame(mainBundle.getString("gui.title") + " v" + version);
        frame.setIconImage(ImageIO.read(MyTunesRss.class.getResource("FrameIcon.png")));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Settings settingsForm = new Settings(frame);
        frame.addWindowListener(new MyTunesRssMainWindowListener(settingsForm));
        frame.getContentPane().add(settingsForm.getRootPanel());
        frame.setResizable(false);
        int x = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_x", frame.getLocation().x);
        int y = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_y", frame.getLocation().y);
        frame.setLocation(x, y);
        frame.pack();
        activateAppleExtensions(frame, settingsForm);
        frame.setVisible(true);
    }

    private static void activateAppleExtensions(final JFrame frame, final Settings settings) {
        try {
            Class.forName("com.apple.eawt.Application");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Running an Apple JRE.");
            }
            try {
                Class clazz = Class.forName("de.codewave.mytunesrss.AppleExtensions");
                Method method = clazz.getMethod("activate", JFrame.class, Settings.class);
                method.invoke(null, frame, settings);
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not activate Apple extensions.", e);
                }
            }
        } catch (ClassNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Not running an Apple JRE.");
            }
        }
    }

    public static class MyTunesRssMainWindowListener extends WindowAdapter {
        private Settings mySettingsForm;

        public MyTunesRssMainWindowListener(Settings settingsForm) {
            mySettingsForm = settingsForm;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            mySettingsForm.doQuitApplication();
        }
    }
}