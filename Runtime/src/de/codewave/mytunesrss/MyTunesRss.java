/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.*;
import de.codewave.utils.moduleinfo.*;
import org.apache.catalina.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.prefs.*;
import java.net.*;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    private static final Log LOG = LogFactory.getLog(MyTunesRss.class);
    public static boolean REGISTERED;

    public static void main(String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException,
            ClassNotFoundException, IOException {
        if (ProgramUtils.getCommandLineArguments(args).containsKey("debug")) {
            Logger.getLogger("de.codewave").setLevel(Level.DEBUG);
        }
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ResourceBundle mainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        String version = modulesInfo.getVersion();
        System.setProperty("mytunesrss.version", version);
        final JFrame frame = new JFrame(mainBundle.getString("gui.title") + " v" + version);
        frame.setIconImage(ImageIO.read(MyTunesRss.class.getResource("WindowIcon.png")));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Settings settingsForm = new Settings(frame);
        frame.addWindowListener(new MyTunesRssMainWindowListener(settingsForm));
        frame.getContentPane().add(settingsForm.getRootPanel());
        frame.setResizable(false);
        int x = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_x", frame.getLocation().x);
        int y = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_y", frame.getLocation().y);
        frame.setLocation(x, y);
        frame.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
            }
        });
    }

    public static String[] getLocalAddresses() throws IOException {
        List<String> localAddresses = new ArrayList<String>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface intFace = interfaces.nextElement();
            Enumeration<InetAddress> addresses = intFace.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isSiteLocalAddress()) {
                    localAddresses.add(address.getHostAddress());
                }
            }
        }
        return localAddresses.toArray(new String[localAddresses.size()]);
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