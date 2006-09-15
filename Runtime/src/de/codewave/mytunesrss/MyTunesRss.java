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
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;
import snoozesoft.systray4j.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.sql.*;
import java.util.*;
import java.util.Timer;
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
    public static Timer DATABASE_WATCHDOG = new Timer("MyTunesRSSDatabaseWatchdog");
    public static SysTray SYSTRAYMENU;
    public static MessageDigest MESSAGE_DIGEST;
    public static String HEADLESS_SYNCHRONIZER = "";

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
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create message digest.", e);
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
            LOG.info("Java: " + getJavaEnvironment());
        }
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        VERSION = modulesInfo != null ? modulesInfo.getVersion() : System.getProperty("MyTunesRSS.version", "0.0.0");
        if (LOG.isInfoEnabled()) {
            LOG.info("Application version: " + VERSION);
        }
        System.setProperty("mytunesrss.version", VERSION);
        if (ProgramUtils.getCommandLineArguments(args).containsKey("headless")) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Headless mode");
            }
            CONFIG.load();
            migrateConfig();
            STORE.init();
            Map<String, Object> contextEntries = new HashMap<String, Object>();
            contextEntries.put(MyTunesRssConfig.class.getName(), CONFIG);
            contextEntries.put(DataStore.class.getName(), STORE);
            URL libraryUrl = new File(CONFIG.getLibraryXml().trim()).toURL();
            new DatabaseBuilderTask(libraryUrl, null).execute();
            final int serverPort = CONFIG.getPort();
            WEBSERVER.start(serverPort, contextEntries);
            if (!WEBSERVER.isRunning()) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(MyTunesRss.WEBSERVER.getLastErrorMessage());
                }
            } else {
                if (CONFIG.isAutoUpdateDatabase()) {
                    int interval = CONFIG.getAutoUpdateDatabaseInterval();
                    DATABASE_WATCHDOG.schedule(new DatabaseWatchdogTask(DATABASE_WATCHDOG, null, interval * 60, libraryUrl), 1000 * interval);
                }
            }
            while (WEBSERVER.isRunning()) {
                try {
                    synchronized(HEADLESS_SYNCHRONIZER) {
                        HEADLESS_SYNCHRONIZER.wait();
                    }
                } catch (InterruptedException e) {
                    // intentionally left blank
                }
            }
        } else {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            final JFrame frame = new JFrame(BUNDLE.getString("settings.title") + " v" + VERSION);
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler(frame));
            PleaseWait.start(frame,
                             BUNDLE.getString("pleaseWait.initializingTitle"),
                             BUNDLE.getString("pleaseWait.initializingMessage"),
                             false,
                             false,
                             new PleaseWait.NoCancelTask() {
                                 public void execute() throws Exception {
                                     CONFIG.load();
                                     migrateConfig();
                                     STORE.init();
                                     final Settings settings = new Settings();
                                     settings.init(frame);
                                     MyTunesRssMainWindowListener mainWindowListener = new MyTunesRssMainWindowListener(settings);
                                     executeApple(mainWindowListener);
                                     executeWindows(settings, mainWindowListener);
                                     frame.setIconImage(ImageIO.read(MyTunesRss.class.getResource("WindowIcon.png")));
                                     frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                                     frame.addWindowListener(mainWindowListener);
                                     frame.getContentPane().add(settings.getRootPanel());
                                     frame.setResizable(false);
                                     final Point defaultPosition = frame.getLocation();
                                     frame.setLocation(1000000, 1000000);
                                     settings.setGuiMode(GuiMode.ServerIdle);
                                     removeAllEmptyTooltips(frame.getRootPane());
                                     frame.setVisible(true);
                                     SwingUtilities.invokeLater(new Runnable() {
                                         public void run() {
                                             frame.pack();
                                             int x = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_x", defaultPosition.x);
                                             int y = Preferences.userRoot().node("/de/codewave/mytunesrss").getInt("window_y", defaultPosition.y);
                                             frame.setLocation(x, y);
                                             if (CONFIG.isCheckUpdateOnStart()) {
                                                 new Updater(frame).checkForUpdate(true);
                                             }
                                             PleaseWait.start(frame,
                                                              null,
                                                              MyTunesRss.BUNDLE.getString("pleaseWait.checkingDatabase"),
                                                              false,
                                                              false,
                                                              new InitializeDatabaseTask());
                                             if (CONFIG.isAutoStartServer()) {
                                                 settings.doStartServer();
                                                 if (ProgramUtils.guessOperatingSystem() == OperatingSystem.MacOSX) {
                                                     // todo: hide window on osx instead of iconify
                                                     frame.setExtendedState(JFrame.ICONIFIED);
                                                 } else {
                                                     frame.setExtendedState(JFrame.ICONIFIED);
                                                 }
                                             }
                                         }
                                     });
                                 }
                             });
        }
    }

    private static String getJavaEnvironment() {
        StringBuffer java = new StringBuffer();
        java.append(System.getProperty("java.version")).append(" (\"").append(System.getProperty("java.home")).append("\")");

        return java.toString();
    }

    private static void migrateConfig() {
        if (CONFIG.getVersion().compareTo("2.1") < 0) {
            // migrate to 2.1
            int autoUpdateDatabaseInterval = CONFIG.getAutoUpdateDatabaseInterval() / 60;
            CONFIG.setAutoUpdateDatabaseInterval(autoUpdateDatabaseInterval > 0 ? autoUpdateDatabaseInterval : 1);
            CONFIG.setVersion("2.1");
        }
    }

    private static void removeAllEmptyTooltips(JComponent component) {
        String toolTipText = component.getToolTipText();
        if (toolTipText != null && StringUtils.isEmpty(toolTipText.trim())) {
            component.setToolTipText(null);
        }
        Component[] childComponents = component.getComponents();
        for (int i = 0; i < childComponents.length; i++) {
            removeAllEmptyTooltips((JComponent)childComponents[i]);
        }
    }

    private static void executeWindows(Settings settingsForm, WindowListener windowListener) {
        if (ProgramUtils.guessOperatingSystem() == OperatingSystem.Windows && SysTrayMenu.isAvailable()) {
            SYSTRAYMENU = new SysTray(settingsForm);
        }
    }

    private static void executeApple(MyTunesRssMainWindowListener mainWindowListener) {
        if (ProgramUtils.guessOperatingSystem() == OperatingSystem.MacOSX) {
            try {
                Class appleExtensionsClass = Class.forName("de.codewave.mytunesrss.AppleExtensions");
                Method activateMethod = appleExtensionsClass.getMethod("activate", WindowListener.class);
                activateMethod.invoke(null, mainWindowListener);
            } catch (Exception e) {
                // intentionally left blank
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

        @Override
        public void windowIconified(WindowEvent e) {
            if (MyTunesRss.SYSTRAYMENU != null) {
                mySettingsForm.getFrame().setVisible(false);
                MyTunesRss.SYSTRAYMENU.show();
            }
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
                if (LOG.isErrorEnabled()) {
                    LOG.error(e);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        myPane.setMessage(MyTunesRss.BUNDLE.getString("error.outOfMemory"));
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
                        myPane.setMessage(MyTunesRss.BUNDLE.getString("error.uncaughtException"));
                        myDialog.pack();
                        myDialog.setLocationRelativeTo(myDialog.getParent());
                        myDialog.setVisible(true);
                    }
                });
            }
        }
    }
}