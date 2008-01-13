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
import de.codewave.utils.io.*;
import de.codewave.utils.maven.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.xml.*;
import org.apache.catalina.*;
import org.apache.commons.io.*;
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

    static {
        try {
            System.setProperty("MyTunesRSS.logDir", PrefsUtils.getCacheDataPath(APPLICATION_IDENTIFIER));
        } catch (IOException e) {
            System.setProperty("MyTunesRSS.logDir", ".");
        }
    }

    private static final Log LOG = LogFactory.getLog(MyTunesRss.class);

    static {
        try {
            File file = new File(PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/system.properties");
            if (file.isFile()) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(file));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Setting system properties from \"" + file.getAbsolutePath() + "\".");
                }
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    System.setProperty(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not load user system properties: " + e.getMessage());
            }
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

    public static final String MYTUNESRSSCOM_URL = "http://mytunesrss.com";
    public static final String MYTUNESRSSCOM_TOOLS_URL = MYTUNESRSSCOM_URL + "/tools";
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
    public static FileCache STREAMING_CACHE;

    public static void main(final String[] args) throws LifecycleException, IllegalAccessException, UnsupportedLookAndFeelException,
            InstantiationException, ClassNotFoundException, IOException, SQLException {
        final Map<String, String[]> arguments = ProgramUtils.getCommandLineArguments(args);
        HEADLESS = arguments.containsKey("headless");
        if (arguments.containsKey("debug")) {
            Logger.getLogger("de.codewave").setLevel(Level.DEBUG);
        }
        MyTunesRssRegistration.RegistrationResult registrationResult = REGISTRATION.init(null, true);
        registerDatabaseDriver();
        VERSION = MavenUtils.getVersion("de.codewave.mytunesrss", "runtime");
        if (StringUtils.isEmpty(VERSION)) {
            VERSION = System.getProperty("MyTunesRSS.version", "0.0.0");
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Operating system: " + SystemUtils.OS_NAME + ", " + SystemUtils.OS_VERSION + ", " + SystemUtils.OS_ARCH);
            LOG.info("Java: " + SystemUtils.JAVA_VERSION + "(" + SystemUtils.JAVA_HOME + ")");
            LOG.info("Application version: " + VERSION);
            LOG.info("Cache data path: " + PrefsUtils.getCacheDataPath(APPLICATION_IDENTIFIER));
            LOG.info("Preferences data path: " + PrefsUtils.getPreferencesDataPath(APPLICATION_IDENTIFIER));
            LOG.info("--------------------------------------------------------------------------------");
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                LOG.info(entry.getKey() + "=" + entry.getValue());
            }
            LOG.info("--------------------------------------------------------------------------------");
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
        if (registrationResult == MyTunesRssRegistration.RegistrationResult.InternalExpired) {
            MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.defaulRegistrationExpired"));
            MyTunesRssUtils.shutdown();
        } else if (registrationResult == MyTunesRssRegistration.RegistrationResult.ExternalExpired) {
            MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.registrationExpired"));
        }
        if (System.getProperty("database.type") == null && Preferences.userRoot().node(MyTunesRssConfig.PREF_ROOT).getBoolean(
                "deleteDatabaseOnNextStartOnError",
                false)) {
            new DeleteDatabaseFilesTask().execute();
        }
        loadConfiguration();
        STREAMING_CACHE = FileCache.createCache(APPLICATION_IDENTIFIER, 10000, CONFIG.getStreamingCacheMaxFiles());
        File streamingCacheFile = new File(PrefsUtils.getCacheDataPath(APPLICATION_IDENTIFIER) + "/transcoder/cache.xml");
        if (streamingCacheFile.isFile()) {
            STREAMING_CACHE.setContent(JXPathUtils.getContext(streamingCacheFile.toURL()));
        }
        if (MyTunesRss.REGISTRATION.isRegistered()) {
            MyTunesRssJmxUtils.startJmxServer();
        }
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

    private static void registerDatabaseDriver()
            throws IOException, SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        File libDir = new File(PrefsUtils.getPreferencesDataPath(APPLICATION_IDENTIFIER) + "/lib");
        Collection<File> files = libDir.isDirectory() ? (Collection<File>)FileUtils.listFiles(libDir, new String[] {"jar"}, false) : null;
        String driverClassName = MyTunesRss.REGISTRATION.isRegistered() ? System.getProperty("database.driver", "org.h2.Driver") : "org.h2.Driver";
        if (files != null && !files.isEmpty()) {
            Collection<URL> urls = new ArrayList<URL>();
            for (File file : files) {
                urls.add(file.toURL());
            }
            final ClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
            try {
                final Class<Driver> driverClass = (Class<Driver>)Class.forName(driverClassName, true, classLoader);
                DriverManager.registerDriver(new Driver() {
                    private Driver myDriver = driverClass.newInstance();

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
            } catch (ClassNotFoundException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Database driver class not found.", e);
                }
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.databaseDriverNotFound",
                                                                                 driverClassName,
                                                                                 libDir.getAbsolutePath()));
                MyTunesRssUtils.shutdown();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(null, e);
                }

            }
        } else {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Database driver class not found.", e);
                }
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.databaseDriverNotFound",
                                                                                 driverClassName,
                                                                                 libDir.getAbsolutePath()));
                MyTunesRssUtils.shutdown();
            }
        }
    }

    public static DatabaseBuilderTask createDatabaseBuilderTask() {
        DatabaseBuilderTask task;
        if (DATABASE_FORM == null) {
            task = new DatabaseBuilderTask();
        } else {
            task = new GuiDatabaseBuilderTask(SETTINGS);
        }
        return task;
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
            if (SystemUtils.IS_OS_MAC_OSX) {
                ROOT_FRAME.setVisible(false);
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
            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.unregisteredNoHeadlessMode"));
        }
        SERVER_RUNNING_TIMER.cancel();
        STORE.destroy();
    }

    public static void startWebserver() {
        if (MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart()) {
            MyTunesRssUtils.executeDatabaseUpdate();
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
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.SERVER_STARTED);
        }
    }

    public static void stopWebserver() {
        MyTunesRssUtils.executeTask(null, MyTunesRssUtils.getBundleString("pleaseWait.serverstopping"), null, false, new MyTunesRssTask() {
            public void execute() throws Exception {
                MyTunesRss.WEBSERVER.stop();
            }
        });
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.SERVER_RUNNING_TIMER.cancel();
            MyTunesRss.SERVER_RUNNING_TIMER = new Timer("MyTunesRSSServerRunningTimer");
            MulticastService.stopListener();
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.SERVER_STOPPED);
        }
    }

    private static void loadConfiguration() throws MalformedURLException {
        CONFIG.loadFromPrefs();
    }

    private static void executeWindows(Settings settingsForm) {
        if (SystemUtils.IS_OS_WINDOWS && SysTrayMenu.isAvailable()) {
            SYSTRAYMENU = new SysTray(settingsForm);
        }
    }

    private static void executeApple(final Settings settings) {
        if (SystemUtils.IS_OS_MAC_OSX) {
            try {
                Class appleExtensionsClass = Class.forName("de.codewave.mytunesrss.AppleExtensions");
                Method activateMethod = appleExtensionsClass.getMethod("activate", EventListener.class);
                activateMethod.invoke(null, new EventListener() {
                    public void handleQuit() {
                        settings.doQuitApplication();
                    }

                    public void handleReOpenApplication() {
                        if (!ROOT_FRAME.isVisible()) {
                            ROOT_FRAME.setVisible(true);
                        }
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
                return MyTunesRssUtils.getBundleString("confirmation.quitMyTunesRss.option" + name());
            }
        }

        private Settings mySettingsForm;

        public MyTunesRssMainWindowListener(Settings settingsForm) {
            mySettingsForm = settingsForm;
        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (SystemUtils.IS_OS_MAC_OSX) {
                ROOT_FRAME.setVisible(false);
            } else {
                if (CONFIG.isQuitConfirmation()) {
                    int result = JOptionPane.showOptionDialog(ROOT_FRAME,
                                                              MyTunesRssUtils.getBundleString("confirmation.quitMyTunesRss"),
                                                              MyTunesRssUtils.getBundleString("pleaseWait.defaultTitle"),
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