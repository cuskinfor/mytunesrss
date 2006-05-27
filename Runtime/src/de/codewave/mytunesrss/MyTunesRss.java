/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.server.*;
import de.codewave.mytunesrss.settings.*;
import de.codewave.mytunesrss.task.*;
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
    public static MyTunesRssConfig CONFIG = new MyTunesRssConfig();
    public static ResourceBundle BUNDLE = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
    public static WebServer WEBSERVER = new WebServer();

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
        if (ProgramUtils.getCommandLineArguments(args).containsKey("debug")) {
            Logger.getLogger("de.codewave").setLevel(Level.DEBUG);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Operating system: " + ProgramUtils.guessOperatingSystem().name());
        }
        CONFIG.load();
        STORE.init();
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        VERSION = modulesInfo != null ? modulesInfo.getVersion() : "0.0.0";
        System.setProperty("mytunesrss.version", VERSION);
        final JFrame frame = new JFrame(BUNDLE.getString("gui.title") + " v" + VERSION);
        frame.setIconImage(ImageIO.read(MyTunesRss.class.getResource("WindowIcon.png")));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        final Settings settings = new Settings();
        settings.init(frame);
        settings.setGuiMode(GuiMode.ServerIdle);
        frame.addWindowListener(new MyTunesRssMainWindowListener(settings));
        frame.getContentPane().add(settings.getRootPanel());
        frame.setResizable(false);
        int x = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_x", frame.getLocation().x);
        int y = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_y", frame.getLocation().y);
        frame.setLocation(x, y);
        frame.setVisible(true);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler(frame));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                if (CONFIG.isCheckUpdateOnStart()) {
                    new Updater(frame).checkForUpdate(true);
                }
                PleaseWait.start(frame, null, "Checking database... please wait.", false, false, new InitializeDatabaseTask());
                if (CONFIG.isAutoStartServer()) {
                    settings.getGeneralForm().doStartServer();
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

    public static class UncaughtHandler implements Thread.UncaughtExceptionHandler {
        private JDialog myDialog;
        private JOptionPane myPane;

        public UncaughtHandler(JFrame parent) {
            myPane = new JOptionPane() {
                @Override
                public int getMaxCharactersPerLineCount() {
                    return 100;
                }
            };
            myPane.setMessageType(JOptionPane.ERROR_MESSAGE);
            String okButton = "Ok";
            myPane.setInitialValue(okButton);
            myDialog = myPane.createDialog(parent, "Fatal error");
            myDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        }

        public void uncaughtException(Thread t, final Throwable e) {
            if (e instanceof OutOfMemoryError) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        myPane.setMessage(
                                "The application has failed because it has run out of memory. Please raise the available memory on the first settings tab and restart MyTunesRSS to activate the changes.");
                        myDialog.pack();
                        myDialog.setLocationRelativeTo(myDialog.getParent());
                        myDialog.setVisible(true);
                    }
                });
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(null, e);
                        }
                        myPane.setMessage(
                                "An error occured. You may be able to continue working but you should check your log file and contact the Codewave support.");
                        myDialog.pack();
                        myDialog.setLocationRelativeTo(myDialog.getParent());
                        myDialog.setVisible(true);
                    }
                });
            }
        }
    }
}