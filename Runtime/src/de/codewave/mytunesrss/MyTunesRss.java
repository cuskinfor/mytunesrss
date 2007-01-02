/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.server.WebServer;
import de.codewave.mytunesrss.settings.General;
import de.codewave.mytunesrss.settings.GuiDatabaseBuilderTask;
import de.codewave.mytunesrss.settings.GuiMode;
import de.codewave.mytunesrss.settings.Settings;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.mytunesrss.task.DeleteDatabaseTask;
import de.codewave.mytunesrss.task.InitializeDatabaseTask;
import de.codewave.utils.OperatingSystem;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.ProgramUtils;
import de.codewave.utils.moduleinfo.ModuleInfo;
import de.codewave.utils.moduleinfo.ModuleInfoUtils;
import de.codewave.utils.swing.SwingUtils;
import org.apache.catalina.LifecycleException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import snoozesoft.systray4j.SysTrayMenu;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;
import java.util.Timer;
import java.util.prefs.Preferences;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    public static final String APPLICATION_IDENTIFIER = "MyTunesRSS30Beta";

    static {
        try {
            System.setProperty("MyTunesRSS.logDir", PrefsUtils.getCacheDataPath(APPLICATION_IDENTIFIER));
        } catch (IOException e) {
            System.setProperty("MyTunesRSS.logDir", ".");
        }
    }

    private static final Log LOG = LogFactory.getLog(MyTunesRss.class);
    public static String VERSION;
    public static URL UPDATE_URL;
    public static MyTunesRssDataStore STORE = new MyTunesRssDataStore();
    public static MyTunesRssConfig CONFIG = new MyTunesRssConfig();
    public static ResourceBundle BUNDLE = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
    public static WebServer WEBSERVER = new WebServer();
    public static Timer SERVER_RUNNING_TIMER = new Timer("MyTunesRSSServerRunningTimer");
    public static SysTray SYSTRAYMENU;
    public static MessageDigest MESSAGE_DIGEST;
    public static JFrame ROOT_FRAME;
    public static JFrame DUMMY_FRAME;
    public static ImageIcon PLEASE_WAIT_ICON;
    public static MyTunesRssRegistration REGISTRATION = new MyTunesRssRegistration();
    public static int OPTION_PANE_MAX_MESSAGE_LENGTH = 100;
    public static boolean HEADLESS;
    private static General GENERAL_FORM;

    static {
        try {
            UPDATE_URL = new URL("http://www.codewave.de/download/versions/mytunesrss.xml");
        } catch (MalformedURLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create update url.", e);
            }
        }
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create message digest.", e);
            }
        }
    }

    public static void main(final String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException,
        InstantiationException, ClassNotFoundException, IOException, SQLException {
        final Map<String, String[]> arguments = ProgramUtils.getCommandLineArguments(args);
        HEADLESS = arguments.containsKey("headless");
        if (arguments.containsKey("debug")) {
            Logger.getLogger("de.codewave").setLevel(Level.DEBUG);
        }
        ModuleInfo modulesInfo = ModuleInfoUtils.getModuleInfo("META-INF/codewave-version.xml", "MyTunesRSS");
        VERSION = modulesInfo != null ? modulesInfo.getVersion() : System.getProperty("MyTunesRSS.version", "0.0.0");
        if (LOG.isInfoEnabled()) {
            LOG.info("Operating system: " + ProgramUtils.guessOperatingSystem().name());
            LOG.info("Java: " + getJavaEnvironment());
            LOG.info("Application version: " + VERSION);
            LOG.info("Cache data path: " + PrefsUtils.getCacheDataPath(APPLICATION_IDENTIFIER));
            LOG.info("Preferences data path: " + PrefsUtils.getPreferencesDataPath(APPLICATION_IDENTIFIER));
        }
        if (!HEADLESS) {
            Thread.setDefaultUncaughtExceptionHandler(new MyTunesRssUncaughtHandler(ROOT_FRAME, false));
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            ROOT_FRAME = new JFrame(BUNDLE.getString("settings.title") + " v" + VERSION);
            ROOT_FRAME.setIconImage(ImageIO.read(MyTunesRss.class.getResource("WindowIcon.png")));
            ROOT_FRAME.setLocation(Integer.MAX_VALUE, 0);
            ROOT_FRAME.setVisible(true);
            ROOT_FRAME.setVisible(false);
            DUMMY_FRAME = new JFrame(BUNDLE.getString("settings.title") + " v" + VERSION);
            DUMMY_FRAME.setIconImage(ImageIO.read(MyTunesRss.class.getResource("WindowIcon.png")));
            DUMMY_FRAME.setLocation(Integer.MAX_VALUE, 0);
            DUMMY_FRAME.setVisible(true);
            PLEASE_WAIT_ICON = new ImageIcon(MyTunesRss.class.getResource("PleaseWait.gif"));
        }
        if (isOtherInstanceRunning(3000)) {
            MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.otherInstanceRunning"));
            MyTunesRssUtils.shutdown();
        }
        REGISTRATION.init();
        if (REGISTRATION.isExpired()) {
            if (REGISTRATION.isDefaultData()) {
                MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.defaulRegistrationExpired"));
                MyTunesRssUtils.shutdown();
            } else {
                MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.registrationExpired"));
            }
        }
        if (Preferences.userRoot().node("/de/codewave/mytunesrss").getBoolean("deleteDatabaseOnNextStartOnError", false)) {
            new DeleteDatabaseTask(false).execute();
        }
        loadConfiguration(arguments);
        if (HEADLESS) {
            executeHeadlessMode();
        } else {
            Thread.setDefaultUncaughtExceptionHandler(new MyTunesRssUncaughtHandler(ROOT_FRAME, false));
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        executeGuiMode();
                    } catch (Exception e) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error(null, e);
                        }
                    }
                }
            });
        }
    }

    public static DatabaseBuilderTask createDatabaseBuilderTask() {
        if (GENERAL_FORM == null) {
            return new DatabaseBuilderTask();
        }
        return new GuiDatabaseBuilderTask(GENERAL_FORM);
    }

    private static boolean isOtherInstanceRunning(long timeoutMillis) {
        RandomAccessFile lockFile;
        try {
            File file = new File(PrefsUtils.getCacheDataPath(APPLICATION_IDENTIFIER) + "/MyTunesRSS.lck");
            file.deleteOnExit();
            lockFile = new RandomAccessFile(file, "rw");
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not check for other running instance.", e);
            }
            return false;
        }
        long endTime = System.currentTimeMillis() + timeoutMillis;
        do {
            try {
                if (lockFile.getChannel().tryLock() != null) {
                    return false;
                }
                Thread.sleep(500);
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not check for other running instance.", e);
                }
            } catch (InterruptedException e) {
                // intentionally left blank
            }
        } while (System.currentTimeMillis() < endTime);
        return true;
    }

    private static String getJavaEnvironment() {
        StringBuffer java = new StringBuffer();
        java.append(System.getProperty("java.version")).append(" (\"").append(System.getProperty("java.home")).append("\")");
        return java.toString();
    }

    private static void executeGuiMode() throws IllegalAccessException, UnsupportedLookAndFeelException, InstantiationException,
        ClassNotFoundException, IOException, InterruptedException {
        showNewVersionInfo();
        final Settings settings = new Settings();
        GENERAL_FORM = settings.getGeneralForm();
        MyTunesRssMainWindowListener mainWindowListener = new MyTunesRssMainWindowListener(settings);
        executeApple(settings);
        executeWindows(settings);
        ROOT_FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ROOT_FRAME.addWindowListener(mainWindowListener);
        ROOT_FRAME.getContentPane().add(settings.getRootPanel());
        ROOT_FRAME.setResizable(false);
        settings.setGuiMode(GuiMode.ServerIdle);
        SwingUtils.removeEmptyTooltips(ROOT_FRAME.getRootPane());
        int x = CONFIG.loadWindowPosition().x;
        int y = CONFIG.loadWindowPosition().y;
        DUMMY_FRAME.dispose();
        if (x != Integer.MAX_VALUE && y != Integer.MAX_VALUE) {
            ROOT_FRAME.setLocation(x, y);
            SwingUtils.packAndShow(ROOT_FRAME);
        } else {
            SwingUtils.packAndShowRelativeTo(ROOT_FRAME, null);
        }
        if (CONFIG.isCheckUpdateOnStart()) {
            UpdateUtils.checkForUpdate(true);
        }
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.initializingDatabase"), null, false, new InitializeDatabaseTask());
        settings.init();
        if (CONFIG.isAutoStartServer()) {
            settings.doStartServer();
            if (ProgramUtils.guessOperatingSystem() == OperatingSystem.MacOSX) {
                // todo: hide window on osx instead of iconify
                ROOT_FRAME.setExtendedState(JFrame.ICONIFIED);
            } else {
                ROOT_FRAME.setExtendedState(JFrame.ICONIFIED);
            }
        }
    }

    private static void showNewVersionInfo() {
        String lastNewVersionInfo = Preferences.userRoot().node("/de/codewave/mytunesrss").get("lastNewVersionInfo", "0");
        if (!VERSION.equals(lastNewVersionInfo)) {
            try {
                String message = BUNDLE.getString("info.newVersion");
                if (StringUtils.isNotEmpty(message)) {
                    MyTunesRssUtils.showInfoMessage(ROOT_FRAME, message);
                }
            } catch (MissingResourceException e) {
                // intentionally left blank
            }
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("lastNewVersionInfo", VERSION);
        }
    }

    private static void executeHeadlessMode() throws IOException, SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Headless mode");
        }
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.initializingDatabase"), null, false, new InitializeDatabaseTask());
        startWebserver();
        if (!WEBSERVER.isRunning()) {
            CONFIG.save();
            SERVER_RUNNING_TIMER.cancel();
            STORE.destroy();
        }
    }

    public static void startWebserver() {
      if (MyTunesRss.CONFIG.isUpdateOnServerStart()) {
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.buildDatabase"), null, false, MyTunesRss.createDatabaseBuilderTask());
      }
      MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.serverstarting"), null, false, new MyTunesRssTask() {
            public void execute() throws Exception {
                WEBSERVER.start();
            }
        });
        if (WEBSERVER.isRunning()) {
            if (CONFIG.isAutoUpdateDatabase()) {
                DatabaseWatchdogTask databaseWatchdogTask = new DatabaseWatchdogTask(SERVER_RUNNING_TIMER,
                                                                                     CONFIG.getAutoUpdateDatabaseInterval());
                SERVER_RUNNING_TIMER.schedule(databaseWatchdogTask, 60000 * CONFIG.getAutoUpdateDatabaseInterval());
            }
            if (StringUtils.isNotEmpty(CONFIG.getMyTunesRssComUser()) && CONFIG.getMyTunesRssComPasswordHash() != null && CONFIG.getMyTunesRssComPasswordHash().length > 0) {
                MyTunesRssComUpdateTask myTunesRssComUpdater = new MyTunesRssComUpdateTask(SERVER_RUNNING_TIMER, 60000, CONFIG.getMyTunesRssComUser(), CONFIG.getMyTunesRssComPasswordHash());
                SERVER_RUNNING_TIMER.schedule(myTunesRssComUpdater, 0);
            }
        }
    }

  public static void stopWebserver() {
    MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.serverstopping"), null, false, new MyTunesRssTask() {
        public void execute() throws Exception {
            MyTunesRss.WEBSERVER.stop();
        }
    });

  }

    private static void loadConfiguration(Map<String, String[]> arguments) throws MalformedURLException {
        if (arguments.containsKey("config")) {
            CONFIG.loadFromXml(new File(arguments.get("config")[0]).toURL());
        } else {
            CONFIG.loadFromPrefs();
        }
    }

    private static void executeWindows(Settings settingsForm) {
        if (ProgramUtils.guessOperatingSystem() == OperatingSystem.Windows && SysTrayMenu.isAvailable()) {
            SYSTRAYMENU = new SysTray(settingsForm);
        }
    }

    private static void executeApple(final Settings settings) {
        if (ProgramUtils.guessOperatingSystem() == OperatingSystem.MacOSX) {
            try {
                Class appleExtensionsClass = Class.forName("de.codewave.mytunesrss.AppleExtensions");
                Method activateMethod = appleExtensionsClass.getMethod("activate", EventListener.class);
                activateMethod.invoke(null, new EventListener() {
                    public void handleQuit() {
                        settings.doQuitApplication();
                    }
                });
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not activate apple extensions.", e);
                }
            }
        }
    }

    private static class MyTunesRssMainWindowListener extends WindowAdapter {
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
            if (SYSTRAYMENU != null) {
                ROOT_FRAME.setVisible(false);
                SYSTRAYMENU.show();
            }
        }
    }
}