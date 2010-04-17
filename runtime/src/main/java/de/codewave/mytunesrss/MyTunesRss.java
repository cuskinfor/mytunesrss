/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.job.MyTunesRssJobUtils;
import de.codewave.mytunesrss.network.MulticastService;
import de.codewave.mytunesrss.quicktime.QuicktimePlayer;
import de.codewave.mytunesrss.server.WebServer;
import de.codewave.mytunesrss.statistics.StatisticsDatabaseWriter;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.mytunesrss.task.DeleteDatabaseFilesCallable;
import de.codewave.mytunesrss.task.InitializeDatabaseCallable;
import de.codewave.utils.ProgramUtils;
import de.codewave.utils.Version;
import de.codewave.utils.io.FileCache;
import de.codewave.utils.maven.MavenUtils;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    public static final String APPLICATION_IDENTIFIER = "MyTunesRSS3";
    public static final Map<String, String[]> COMMAND_LINE_ARGS = new HashMap<String, String[]>();
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRss.class);
    public static final String MYTUNESRSSCOM_URL = "http://mytunesrss.com";
    public static final String MYTUNESRSSCOM_TOOLS_URL = MYTUNESRSSCOM_URL + "/tools";
    public static String VERSION;
    public static URL UPDATE_URL;
    public static MyTunesRssDataStore STORE = new MyTunesRssDataStore();
    public static MyTunesRssConfig CONFIG;
    public static ResourceBundle BUNDLE = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.MyTunesRss");
    public static WebServer WEBSERVER = new WebServer();
    public static Timer SERVER_RUNNING_TIMER = new Timer("MyTunesRSSServerRunningTimer");
    public static MessageDigest SHA1_DIGEST;
    public static MessageDigest MD5_DIGEST;
    public static MyTunesRssRegistration REGISTRATION = new MyTunesRssRegistration();
    public static final String THREAD_PREFIX = "MyTunesRSS: ";
    public static boolean QUIT_REQUEST;
    public static FileCache STREAMING_CACHE;
    public static FileCache ARCHIVE_CACHE;
    public static Scheduler QUARTZ_SCHEDULER;
    public static MailSender MAILER = new MailSender();
    public static AdminNotifier ADMIN_NOTIFY = new AdminNotifier();
    public static QuicktimePlayer QUICKTIME_PLAYER;
    public static LuceneTrackService LUCENE_TRACK_SERVICE = new LuceneTrackService();
    public static String[] ORIGINAL_CMD_ARGS;
    public static ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    public static Server ADMIN_SERVER;

    private static void init() {
        try {
            SHA1_DIGEST = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not create SHA-1 digest.", e);
            }
        }
        try {
            MD5_DIGEST = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not create MD5 digest.", e);
            }
        }
        try {
            System.setProperty("MyTunesRSS.logDir", MyTunesRssUtils.getCacheDataPath());
        } catch (IOException e) {
            System.setProperty("MyTunesRSS.logDir", ".");
        }
        try {
            for (Iterator<File> iter =
                    (Iterator<File>) FileUtils.iterateFiles(new File(MyTunesRssUtils.getCacheDataPath()),
                            new String[]{"log"},
                            false); iter.hasNext();) {
                iter.next().delete();
            }
        } catch (Exception e) {
            // ignore exceptions when deleting log files
        }
        DOMConfigurator.configure(MyTunesRss.class.getResource("/mytunesrss-log4j.xml"));
        WEBSERVER = new WebServer();
        MAILER = new MailSender();
        ADMIN_NOTIFY = new AdminNotifier();
        try {
            File file = new File(MyTunesRssUtils.getPreferencesDataPath() + "/system.properties");
            if (file.isFile()) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(file));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Setting system properties from \"" + file.getAbsolutePath() + "\".");
                }
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    System.setProperty(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not load user system properties: " + e.getMessage());
            }
        }
        try {
            UPDATE_URL = new URL("http://www.codewave.de/download/versions/mytunesrss.xml");
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not create update external.", e);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                MyTunesRssUtils.onShutdown();
            }
        }));
        ORIGINAL_CMD_ARGS = args;
        Map<String, String[]> arguments = ProgramUtils.getCommandLineArguments(args);
        if (arguments != null) {
            COMMAND_LINE_ARGS.putAll(arguments);
        }
        if (System.getProperty("de.codewave.mytunesrss.shutdown") == null) {
            init();
        }
        LOGGER.info("Command line: " + StringUtils.join(args, " "));
        VERSION = MavenUtils.getVersion("de.codewave.mytunesrss", "runtime");
        if (StringUtils.isEmpty(VERSION)) {
            VERSION = System.getProperty("MyTunesRSS.version", "0.0.0");
        }

        CONFIG = new MyTunesRssConfig();
        MyTunesRss.CONFIG.load();
        File license = null;
        if (arguments.containsKey("license")) {
            license = new File(arguments.get("license")[0]);
            if (!license.isFile()) {
                LOGGER.error("License file \"" + license.getAbsolutePath() + "\" specified on command line does not exist.");
                license = null;
            } else {
                LOGGER.info("Using license file \"" + license.getAbsolutePath() + "\" specified on command line.");
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting admin server on port " + MyTunesRss.CONFIG.getAdminPort() + ".");
        }

        startAdminServer();

        REGISTRATION = new MyTunesRssRegistration();
        REGISTRATION.init(license, true);
        if (REGISTRATION.getSettings() != null) {
            LOGGER.info("Loading configuration from license.");
            MyTunesRssConfig configFromFile = MyTunesRss.CONFIG;
            MyTunesRss.CONFIG = new MyTunesRssConfig();
            MyTunesRss.CONFIG.loadFromContext(REGISTRATION.getSettings());
            if (configFromFile.getPathInfoKey() != null) {
                MyTunesRss.CONFIG.setPathInfoKey(configFromFile.getPathInfoKey());
            }
        }
        MyTunesRssUtils.setCodewaveLogLevel(MyTunesRss.CONFIG.getCodewaveLogLevel());
        initializeQuicktimePlayer();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Operating system: " + SystemUtils.OS_NAME + ", " + SystemUtils.OS_VERSION + ", " + SystemUtils.OS_ARCH);
            LOGGER.info("Java: " + SystemUtils.JAVA_VERSION + "(" + SystemUtils.JAVA_HOME + ")");
            LOGGER.info("Maximum heap size: " + MyTunesRssUtils.getMemorySizeForDisplay(Runtime.getRuntime().maxMemory()));
            LOGGER.info("Application version: " + VERSION);
            LOGGER.info("Cache data path: " + MyTunesRssUtils.getCacheDataPath());
            LOGGER.info("Preferences data path: " + MyTunesRssUtils.getPreferencesDataPath());
            LOGGER.info("--------------------------------------------------------------------------------");
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                LOGGER.info(entry.getKey() + "=" + entry.getValue());
            }
            LOGGER.info("--------------------------------------------------------------------------------");
        }
        if (System.getProperty("de.codewave.mytunesrss") == null) {
            String type = "generic";
            if (SystemUtils.IS_OS_WINDOWS) {
                type = "windows";
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                type = "osx";
            }
            MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.missingSystemProperty." + type));
        }
        if (MyTunesRssUtils.isOtherInstanceRunning(3000)) {
            MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.otherInstanceRunning"));
            MyTunesRssUtils.shutdownGracefully();
        }
        if (new Version(CONFIG.getVersion()).compareTo(new Version(VERSION)) > 0) {
            MyTunesRssUtils.showErrorMessage(MessageFormat.format(BUNDLE.getString("error.configVersionMismatch"), VERSION, CONFIG.getVersion()));
            MyTunesRssUtils.shutdownGracefully();
        }
        if (REGISTRATION.isExpiredPreReleaseVersion()) {
            MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.preReleaseVersionExpired"));
            MyTunesRssUtils.shutdownGracefully();
        } else if (REGISTRATION.isExpired()) {
            MyTunesRssUtils.showErrorMessage(BUNDLE.getString("error.registrationExpiredHeadless"));
            MyTunesRssUtils.shutdownGracefully();
        } else if (REGISTRATION.isExpirationDate() && !REGISTRATION.isReleaseVersion()) {
            MyTunesRssUtils.showInfoMessage(MyTunesRssUtils.getBundleString("info.preReleaseExpiration",
                    REGISTRATION.getExpiration(MyTunesRssUtils.getBundleString(
                            "common.dateFormat"))));
        } else if (REGISTRATION.isExpirationDate() && !REGISTRATION.isExpired()) {
            MyTunesRssUtils.showInfoMessage(MyTunesRssUtils.getBundleString("info.expirationInfo",
                    REGISTRATION.getExpiration(MyTunesRssUtils.getBundleString(
                            "common.dateFormat"))));
        }
        QUARTZ_SCHEDULER = new StdSchedulerFactory().getScheduler();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting quartz scheduler.");
        }
        QUARTZ_SCHEDULER.start();
        STREAMING_CACHE = FileCache.createCache(APPLICATION_IDENTIFIER + "_Streaming", 10000, CONFIG.getStreamingCacheMaxFiles());
        File streamingCacheFile = new File(MyTunesRssUtils.getCacheDataPath() + "/transcoder/cache.xml");
        if (streamingCacheFile.isFile()) {
            try {
                STREAMING_CACHE.setContent(JXPathUtils.getContext(streamingCacheFile.toURL()));
            } catch (Exception e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Could not read streaming cache file. Starting with empty cache.", e);
                }
                STREAMING_CACHE.clearCache();
            }
        }
        ARCHIVE_CACHE = FileCache.createCache(APPLICATION_IDENTIFIER + "_Archives", 10000, 50); // TODO max size config?
        StatisticsEventManager.getInstance().addListener(new StatisticsDatabaseWriter());
        executeHeadlessMode();
    }

    public static boolean startAdminServer() {
        try {
            ADMIN_SERVER = new Server(MyTunesRss.CONFIG.getAdminPort());
            WebAppContext adminContext = new WebAppContext("webapps/ADMIN", "/");
            ADMIN_SERVER.setHandler(adminContext);
            ADMIN_SERVER.start();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Cannot start admin server.", e);
            }
            return false;
        }
        return true;
    }

    public static boolean stopAdminServer() {
        try {
            ADMIN_SERVER.stop();
            ADMIN_SERVER.join();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Cannot stop admin server.", e);
            }
            return false;
        }
        return true;
    }

    private static void initializeQuicktimePlayer() {
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                // try to find class
                Class.forName("quicktime.util.QTBuild");
                QUICKTIME_PLAYER = new QuicktimePlayer();
                LOGGER.info("Quicktime player created successfully.");
            } catch (ClassNotFoundException e) {
                LOGGER.info("No quicktime environment found, quicktime player disabled.");
            }
        }
    }

    private static ClassLoader createExtraClassloader(File libDir) {
        try {
            Collection<File> files = libDir.isDirectory() ? (Collection<File>) FileUtils.listFiles(libDir, new String[]{"jar", "zip"}, false) : null;
            if (files != null && !files.isEmpty()) {
                Collection<URL> urls = new ArrayList<URL>();
                for (File file : files) {
                    urls.add(file.toURL());
                }
                return new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
            }
        } catch (IOException e) {
            LOGGER.error("Could not create extra classloader.", e);
        }
        return null;
    }

    private static void registerDatabaseDriver()
            throws IOException, SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        File libDir = new File(MyTunesRssUtils.getPreferencesDataPath() + "/lib");
        ClassLoader classLoader = createExtraClassloader(libDir);
        String driverClassName = MyTunesRss.CONFIG.getDatabaseDriver();
        LOGGER.info("Using database driver class \"" + driverClassName + "\".");
        if (classLoader != null) {
            try {
                final Class<Driver> driverClass = (Class<Driver>) Class.forName(driverClassName, true, classLoader);
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
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Database driver class not found.", e);
                }
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.databaseDriverNotFound",
                        driverClassName,
                        libDir.getAbsolutePath()));
                MyTunesRssUtils.shutdownGracefully();
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(null, e);
                }

            }
        } else {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Database driver class not found.", e);
                }
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.databaseDriverNotFound",
                        driverClassName,
                        libDir.getAbsolutePath()));
                MyTunesRssUtils.shutdownGracefully();
            }
        }
    }

    private static void executeHeadlessMode() throws IOException, SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Headless mode");
        }
        while (true) {
            registerDatabaseDriver();
            InitializeDatabaseCallable callable = new InitializeDatabaseCallable();
            callable.call();
            if (callable.getException() != null) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.databaseInitError"));
                if (isAutoResetDatabaseOnError()) {
                    LOGGER.info("Recreating default database.");
                    CONFIG.setDefaultDatabaseSettings();
                    try {
                        new DeleteDatabaseFilesCallable().call();
                        LOGGER.info("Starting retry.");
                        continue; // retry
                    } catch (IOException e) {
                        LOGGER.error("Could not delete database files.");
                        CONFIG.setDeleteDatabaseOnExit(true);
                    }
                }
                MyTunesRssUtils.shutdownGracefully();
            }
            if (callable.getDatabaseVersion().compareTo(new Version(MyTunesRss.VERSION)) > 0) {
                MyTunesRssUtils.showErrorMessage(MessageFormat.format(MyTunesRssUtils.getBundleString("error.databaseVersionMismatch"), MyTunesRss.VERSION, callable.getDatabaseVersion().toString()));
                if (isAutoResetDatabaseOnError()) {
                    LOGGER.info("Recreating default database.");
                    CONFIG.setDefaultDatabaseSettings();
                    try {
                        STORE.destroy();
                        new DeleteDatabaseFilesCallable().call();
                        LOGGER.info("Starting retry.");
                        continue; // retry
                    } catch (IOException e) {
                        LOGGER.error("Could not delete database files.");
                        CONFIG.setDeleteDatabaseOnExit(true);
                    }
                }
                MyTunesRssUtils.shutdownGracefully();
            }
            break; // ok, continue with main flow
        }
        MyTunesRssJobUtils.scheduleStatisticEventsJob();
        MyTunesRssJobUtils.scheduleDatabaseJob();
        startWebserver();
        while (!QUIT_REQUEST) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOGGER.debug("Main thread was interrupted in headless mode.", e);
                QUIT_REQUEST = true;
            }
        }
        LOGGER.debug("Quit request was TRUE.");
        MyTunesRssUtils.shutdownGracefully();
    }

    private static boolean isAutoResetDatabaseOnError() {
        return COMMAND_LINE_ARGS.containsKey("autoResetDatabaseOnError");
    }

    public static void startWebserver() {
        if (MyTunesRss.CONFIG.isUpdateDatabaseOnServerStart()) {
            MyTunesRssUtils.executeDatabaseUpdate();
        }
        WEBSERVER.start();
        if (WEBSERVER.isRunning()) {
            if (MyTunesRss.CONFIG.isMyTunesRssComActive()) {
                MyTunesRssComUpdateTask myTunesRssComUpdater = new MyTunesRssComUpdateTask(SERVER_RUNNING_TIMER,
                        300000,
                        CONFIG.getMyTunesRssComUser(),
                        CONFIG.getMyTunesRssComPasswordHash());
                SERVER_RUNNING_TIMER.schedule(myTunesRssComUpdater, 0);
            }
            if (MyTunesRss.CONFIG.isAvailableOnLocalNet()) {
                MulticastService.startListener();
            }
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.SERVER_STARTED));
        }
    }

    public static void stopWebserver() {
        MyTunesRss.WEBSERVER.stop();
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.SERVER_RUNNING_TIMER.cancel();
            MyTunesRss.SERVER_RUNNING_TIMER = new Timer("MyTunesRSSServerRunningTimer");
            MulticastService.stopListener();
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.SERVER_STOPPED));
        }
    }

}
