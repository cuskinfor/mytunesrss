/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.utils.*;
import de.codewave.utils.moduleinfo.*;
import org.apache.catalina.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    private static final Log LOG = LogFactory.getLog(MyTunesRss.class);
    public static String VERSION;
    public static Map<OperatingSystem, URL> UPDATE_URLS;
    public static DataStore STORE = new DataStore();
    public static boolean NO_FILE_CHECK;

    static {
        UPDATE_URLS = new HashMap<OperatingSystem, URL>();
        String base = "http://www.codewave.de/download/versions/mytunesrss_";
        try {
            UPDATE_URLS.put(OperatingSystem.MacOSX, new URL(base + "macosx.txt"));
            UPDATE_URLS.put(OperatingSystem.Windows, new URL(base + "windows.txt"));
            UPDATE_URLS.put(OperatingSystem.Unknown, new URL(base + "generic.txt"));
        } catch (MalformedURLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create update url.", e);
            }
        }
    }

    public static void main(String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException,
            ClassNotFoundException, IOException, SQLException {
        STORE.init();
        if (ProgramUtils.getCommandLineArguments(args).containsKey("debug")) {
            Logger.getLogger("de.codewave").setLevel(Level.DEBUG);
        }
        NO_FILE_CHECK = ProgramUtils.getCommandLineArguments(args).containsKey("nofilecheck");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ResourceBundle mainBundle = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        VERSION = modulesInfo != null ? modulesInfo.getVersion() : "0.0.0";
        System.setProperty("mytunesrss.version", VERSION);
        final JFrame frame = new JFrame(mainBundle.getString("gui.title") + " v" + VERSION);
        frame.setIconImage(ImageIO.read(MyTunesRss.class.getResource("WindowIcon.png")));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        final Settings settings = new Settings(frame);
        frame.addWindowListener(new MyTunesRssMainWindowListener(settings));
        frame.getContentPane().add(settings.getRootPanel());
        frame.setResizable(false);
        int x = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_x", frame.getLocation().x);
        int y = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_y", frame.getLocation().y);
        frame.setLocation(x, y);
        frame.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                if (settings.isUpdateCheckOnStartup()) {
                    settings.checkForUpdate(true);
                }
                MyTunesRssConfig data = new MyTunesRssConfig();
                data.load();
                if (data.isAutoStartServer()) {
                    settings.doStartServer();
                }
            }
        });
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