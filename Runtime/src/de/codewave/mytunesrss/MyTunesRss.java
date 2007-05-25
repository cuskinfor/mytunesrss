/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.jmx.*;
import de.codewave.mytunesrss.network.*;
import de.codewave.mytunesrss.server.*;
import de.codewave.mytunesrss.settings.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.*;
import de.codewave.utils.moduleinfo.*;
import de.codewave.utils.swing.*;
import org.apache.catalina.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;
import org.apache.log4j.*;
import snoozesoft.systray4j.*;

import javax.imageio.*;
import javax.swing.*;
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
    public static final String APPLICATION_IDENTIFIER = "MyTunesRSS3";
    public static final String MYTUNESRSSCOM_TOOLS_URL = "http://mytunesrss.com/tools";
    private static final Log LOG = LogFactory.getLog(MyTunesRss.class);

    static {
        try {
            System.setProperty("MyTunesRSS.logDir", PrefsUtils.getCacheDataPath(APPLICATION_IDENTIFIER));
        } catch (IOException e) {
            System.setProperty("MyTunesRSS.logDir", ".");
        }
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

    public static String VERSION;
    public static URL UPDATE_URL;
    public static MyTunesRssDataStore STORE = new MyTunesRssDataStore();
    public static MyTunesRssConfig CONFIG = new MyTunesRssConfig();
    public static ResourceBundle BUNDLE = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
    public static ResourceBundle JMX_BUNDLE = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.jmx.MyTunesRssJmx");
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
    private static Database DATABASE_FORM;
    private static Settings SETTINGS;
    public static final String THREAD_PREFIX = "MyTunesRSS: ";
    public static final ErrorQueue ERROR_QUEUE = new ErrorQueue();
    public static boolean QUIT_REQUEST;
    public static Driver DATABASE_DRIVER;

    public static void main(final String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException,
            InstantiationException, ClassNotFoundException, IOException, SQLException {
        final Map<String, String[]> arguments = ProgramUtils.getCommandLineArguments(args);
        HEADLESS = arguments.containsKey("headless");
        if (arguments.containsKey("debug")) {
            Logger.getLogger("de.codewave").setLevel(Level.DEBUG);
        }
        if (arguments.containsKey("lib")) {
            registerDatabaseDriver(arguments.get("lib")[0]);
        } else {
            Class.forName(System.getProperty("database.driver", "org.h2.Driver"));
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
        REGISTRATION.init(null, true);
        if (REGISTRATION.isExpired()) {
            if (REGISTRATION.isDefaultData()) {
                MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.defaulRegistrationExpired"));
                MyTunesRssUtils.shutdown();
            } else {
                MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.registrationExpired"));
            }
        }
        if (Preferences.userRoot().node(MyTunesRssConfig.PREF_ROOT).getBoolean("deleteDatabaseOnNextStartOnError", false)) {
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

    private static void registerDatabaseDriver(String classpath) throws MalformedURLException, SQLException, ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        List<URL> urls = new ArrayList<URL>();
        for (String libPath : StringUtils.split(classpath, ";:")) {
            urls.add(new File(libPath).toURL());
        }
        final ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
        DriverManager.registerDriver(new Driver() {
            private Driver myDriver = (Driver)Class.forName(System.getProperty("database.driver", "org.h2.Driver"), true, classLoader).newInstance();

            public Connection connect(String string, Properties properties) throws SQLException {
                return myDriver.connect(string, properties);
            }

            public boolean acceptsURL(String string) throws SQLException {
                return myDriver.acceptsURL(string);
            }

            public DriverPropertyInfo[] getPropertyInfo(String string, Properties properties) throws SQLException {
                return myDriver.getPropertyInfo(string, properties);
            }

            public int getMajorVersion() {
                return myDriver.getMajorVersion();
            }

            public int getMinorVersion() {
                return myDriver.getMinorVersion();
            }

            public boolean jdbcCompliant() {
                return myDriver.jdbcCompliant();
            }
        });
    }

    public static DatabaseBuilderTask createDatabaseBuilderTask() {
        if (DATABASE_FORM == null) {
            return new DatabaseBuilderTask();
        }
        return new GuiDatabaseBuilderTask(SETTINGS);
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
        SETTINGS = new Settings();
        DATABASE_FORM = SETTINGS.getDatabaseForm();
        MyTunesRssMainWindowListener mainWindowListener = new MyTunesRssMainWindowListener(SETTINGS);
        executeApple(SETTINGS);
        executeWindows(SETTINGS);
        ROOT_FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ROOT_FRAME.addWindowListener(mainWindowListener);
        ROOT_FRAME.getContentPane().add(SETTINGS.getRootPanel());
        ROOT_FRAME.setResizable(false);
        SETTINGS.setGuiMode(GuiMode.ServerIdle);
        SwingUtils.removeEmptyTooltips(ROOT_FRAME.getRootPane());
        int x = CONFIG.loadWindowPosition().x;
        int y = CONFIG.loadWindowPosition().y;
        if (CONFIG.isCheckUpdateOnStart()) {
            UpdateUtils.checkForUpdate(true);
        }
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.initializingDatabase"), null, false, new InitializeDatabaseTask());
        SETTINGS.init();
        DUMMY_FRAME.dispose();
        if (x != Integer.MAX_VALUE && y != Integer.MAX_VALUE) {
            ROOT_FRAME.setLocation(x, y);
            SwingUtils.packAndShow(ROOT_FRAME);
        } else {
            SwingUtils.packAndShowRelativeTo(ROOT_FRAME, null);
        }
        if (CONFIG.isAutoStartServer()) {
            SETTINGS.doStartServer();
            if (ProgramUtils.guessOperatingSystem() == OperatingSystem.MacOSX) {
                // todo: hide window on osx instead of iconify
                ROOT_FRAME.setExtendedState(JFrame.ICONIFIED);
            } else {
                ROOT_FRAME.setExtendedState(JFrame.ICONIFIED);
            }
        }
    }

    private static void showNewVersionInfo() {
        String lastNewVersionInfo = Preferences.userRoot().node(MyTunesRssConfig.PREF_ROOT).get("lastNewVersionInfo", "0");
        if (!VERSION.equals(lastNewVersionInfo)) {
            try {
                String message = BUNDLE.getString("info.newVersion");
                if (StringUtils.isNotEmpty(message)) {
                    MyTunesRssUtils.showInfoMessage(ROOT_FRAME, message);
                }
            } catch (MissingResourceException e) {
                // intentionally left blank
            }
            Preferences.userRoot().node(MyTunesRssConfig.PREF_ROOT).put("lastNewVersionInfo", VERSION);
        }
    }

    private static void executeHeadlessMode() throws IOException, SQLException {
        if (LOG.isInfoEnabled()) {
            LOG.info("Headless mode");
        }
        if (MyTunesRss.REGISTRATION.isRegistered()) {
            MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.initializingDatabase"), null, false, new InitializeDatabaseTask());
            MyTunesRssJmxUtils.startJmxServer();
            if (CONFIG.isAutoStartServer()) {
                startWebserver();
            }
            while (!QUIT_REQUEST) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // intentionally left blank

                }
            }
            MyTunesRssJmxUtils.stopJmxServer();
            CONFIG.save();
        } else {
            MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.unregisteredNoHeadlessMode"));
        }
        SERVER_RUNNING_TIMER.cancel();
        STORE.destroy();
    }

    public static void startWebserver() {
        if (MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart()) {
            MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.buildDatabase"), null, false, MyTunesRss.createDatabaseBuilderTask());
        }
        MyTunesRssUtils.executeTask(null, BUNDLE.getString("pleaseWait.serverstarting"), null, false, new MyTunesRssTask() {
            public void execute() throws Exception {
                WEBSERVER.start();
            }
        });
        if (WEBSERVER.isRunning()) {
            if (CONFIG.isAutoUpdateDatabase()) {
                DatabaseWatchdogTask databaseWatchdogTask = new DatabaseWatchdogTask(SERVER_RUNNING_TIMER, CONFIG.getAutoUpdateDatabaseInterval());
                SERVER_RUNNING_TIMER.schedule(databaseWatchdogTask, 60000 * CONFIG.getAutoUpdateDatabaseInterval());
            }
            if (StringUtils.isNotEmpty(CONFIG.getMyTunesRssComUser()) && CONFIG.getMyTunesRssComPasswordHash() != null &&
                    CONFIG.getMyTunesRssComPasswordHash().length > 0) {
                MyTunesRssComUpdateTask myTunesRssComUpdater = new MyTunesRssComUpdateTask(SERVER_RUNNING_TIMER,
                                                                                           300000,
                                                                                           CONFIG.getMyTunesRssComUser(),
                                                                                           CONFIG.getMyTunesRssComPasswordHash());
                SERVER_RUNNING_TIMER.schedule(myTunesRssComUpdater, 0);
            }
            if (MyTunesRss.CONFIG.isAvailableOnLocalNet()) {
                MulticastService.startListener();
            }
        }
    }

    public static void stopWebserver() {
        MyTunesRssUtils.executeTask(null, MyTunesRss.BUNDLE.getString("pleaseWait.serverstopping"), null, false, new MyTunesRssTask() {
            public void execute() throws Exception {
                MyTunesRss.WEBSERVER.stop();
            }
        });
        MulticastService.stopListener();
    }

    private static void loadConfiguration(Map<String, String[]> arguments) throws MalformedURLException {
        CONFIG.loadFromPrefs();
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
        enum QuitConfirmOption {
            Yes(), No(), NoButMinimize();

            @Override
            public String toString() {
                return MyTunesRss.BUNDLE.getString("confirmation.quitMyTunesRss.option" + name());
            }
        }

        private Settings mySettingsForm;

        public MyTunesRssMainWindowListener(Settings settingsForm) {
            mySettingsForm = settingsForm;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (CONFIG.isQuitConfirmation()) {
                int result = JOptionPane.showOptionDialog(ROOT_FRAME,
                                                          MyTunesRss.BUNDLE.getString("confirmation.quitMyTunesRss"),
                                                          MyTunesRss.BUNDLE.getString("pleaseWait.defaultTitle"),
                                                          JOptionPane.YES_NO_OPTION,
                                                          JOptionPane.QUESTION_MESSAGE,
                                                          null,
                                                          new QuitConfirmOption[] {QuitConfirmOption.NoButMinimize, QuitConfirmOption.No,
                                                                                   QuitConfirmOption.Yes},
                                                          QuitConfirmOption.No);
                if (result == 1) {
                    return;
                } else if (result == 0) {
                    ROOT_FRAME.setExtendedState(JFrame.ICONIFIED);
                    return;
                }
            }
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