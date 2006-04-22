/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.moduleinfo.*;
import de.codewave.utils.serialnumber.*;
import org.apache.catalina.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import javax.imageio.*;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.*;
import java.io.*;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    static final String SER_NUM_RANDOM = "myTUNESrss4eeeever!";
    static boolean REGISTERED;

    public static void main(String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException,
            ClassNotFoundException, IOException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ResourceBundle mainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        String version = modulesInfo.getVersion();
        System.setProperty("mytunesrss.version", version);
        JFrame frame = new JFrame(mainBundle.getString("gui.title") + " v" + version);
        checkRegistration(frame);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Settings settingsForm = new Settings(frame);
        frame.addWindowListener(new MyTunesRssMainWindowListener(settingsForm));
        frame.getContentPane().add(settingsForm.getRootPanel());
        frame.setResizable(false);
        int x = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_x", frame.getLocation().x);
        int y = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_y", frame.getLocation().y);
        frame.setLocation(x, y);
        frame.pack();
        frame.setVisible(true);
    }

    private static void checkRegistration(JFrame frame) throws UnsupportedEncodingException {
        String regName = Preferences.userRoot().node("/de/codewave/mytunesrss").get("regname", "");
        String regCode = Preferences.userRoot().node("/de/codewave/mytunesrss").get("regcode", "0000-0000-0000-0000-0000");
        REGISTERED = SerialNumberUtils.isValid(regName, regCode, SER_NUM_RANDOM);
        if (!REGISTERED) {
            Registration.displayRegistration(frame);
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