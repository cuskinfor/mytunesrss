/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.jna.ffmpeg.HttpLiveStreamingSegmenter;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.desktop.DesktopWrapper;
import de.codewave.mytunesrss.desktop.DesktopWrapperFactory;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingCacheItem;
import de.codewave.mytunesrss.job.MyTunesRssJobUtils;
import de.codewave.mytunesrss.network.MulticastService;
import de.codewave.mytunesrss.quicktime.QuicktimePlayer;
import de.codewave.mytunesrss.server.WebServer;
import de.codewave.mytunesrss.statistics.StatisticsDatabaseWriter;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.mytunesrss.task.DeleteDatabaseFilesCallable;
import de.codewave.mytunesrss.task.InitializeDatabaseCallable;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.ProgramUtils;
import de.codewave.utils.Version;
import de.codewave.utils.cache.ExpiringCache;
import de.codewave.utils.io.FileCache;
import de.codewave.utils.maven.MavenUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.Queue;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    // Specify admin server port on command line (e.g. -adminPort 9090)
    public static final String CMD_ADMIN_PORT = "adminPort";

    // Do not start browser when starting admin server on an non-headless system (e.g. -noBrowser)
    public static final String CMD_NO_BROWSER = "noBrowser";

    // Location of the cache data path (e.g. -cacheDataPath /var/mytunesrss/cache)
    public static final String CMD_CACHE_PATH = "cacheDataPath";

    // Location of the preferfences data path (e.g. -prefDataPath /var/mytunesrss/prefs)
    public static final String CMD_PREFS_PATH = "prefsDataPath";

    // Cache directory names
    public static final String CACHEDIR_TEMP = "tmp";
    public static final String CACHEDIR_TRANSCODER = "transcoder";
    public static final String CACHEDIR_HTTP_LIVE_STREAMING = "http_live_streaming";

    public static final String APPLICATION_IDENTIFIER = "MyTunesRSS4";
    public static final String[] APPLICATION_IDENTIFIER_PREV_VERSIONS = new String[]{"MyTunesRSS3"};
    public static final Map<String, String[]> COMMAND_LINE_ARGS = new HashMap<String, String[]>();
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRss.class);
    public static final String MYTUNESRSSCOM_URL = "http://mytunesrss.com";
    public static final String MYTUNESRSSCOM_TOOLS_URL = MYTUNESRSSCOM_URL + "/tools";
    public static String VERSION;
    public static URL UPDATE_URL;
    public static MyTunesRssDataStore STORE = new MyTunesRssDataStore();
    public static MyTunesRssConfig CONFIG;
    public static WebServer WEBSERVER = new WebServer();
    public static Timer SERVER_RUNNING_TIMER = new Timer("MyTunesRSSServerRunningTimer");
    public static MessageDigest SHA1_DIGEST;
    public static MessageDigest MD5_DIGEST;
    public static MyTunesRssRegistration REGISTRATION = new MyTunesRssRegistration();
    public static final String THREAD_PREFIX = "MyTunesRSS: ";
    public static boolean QUIT_REQUEST;
    public static FileCache STREAMING_CACHE;
    public static FileCache TEMP_CACHE;
    public static ExpiringCache<HttpLiveStreamingCacheItem> HTTP_LIVE_STREAMING_CACHE;
    public static Scheduler QUARTZ_SCHEDULER;
    public static MailSender MAILER = new MailSender();
    public static AdminNotifier ADMIN_NOTIFY = new AdminNotifier();
    public static QuicktimePlayer QUICKTIME_PLAYER;
    public static LuceneTrackService LUCENE_TRACK_SERVICE = new LuceneTrackService();
    public static String[] ORIGINAL_CMD_ARGS;
    public static MyTunesRssExecutorService EXECUTOR_SERVICE = new MyTunesRssExecutorService();
    public static Server ADMIN_SERVER;
    public static Queue<MyTunesRssNotification> NOTIFICATION_QUEUE = new ConcurrentLinkedQueue<MyTunesRssNotification>();
    public static boolean HEADLESS = GraphicsEnvironment.isHeadless();
    public static ResourceBundleManager RESOURCE_BUNDLE_MANAGER = new ResourceBundleManager(MyTunesRss.class.getClassLoader());
    public static boolean HTTP_LIVE_STREAMING_AVAILABLE;
    public static BlockingQueue<IndexedLoggingEvent> LOG_BUFFER = new LinkedBlockingQueue<IndexedLoggingEvent>();

    public static void main(final String[] args) throws Exception {
        /*NOTIFICATION_QUEUE.offer(new MyTunesRssNotification("Test1", "This is a test",  null));
        NOTIFICATION_QUEUE.offer(new MyTunesRssNotification("Second Info", "Wow, it works!",  null));
        NOTIFICATION_QUEUE.offer(new MyTunesRssNotification("Error No. 3", "Mambo NO. 5",  null));*/
        registerShutdownHook();
        processArguments(args);
        copyOldPrefsAndCache();
        createDigests();
        prepareLogging();
        LOGGER.info("Command line: " + StringUtils.join(args, " "));
        WEBSERVER = new WebServer();
        MAILER = new MailSender();
        ADMIN_NOTIFY = new AdminNotifier();
        loadSystemProperties();
        prepareUpdateUrl();
        readVersion();
        loadConfig();
        handleRegistration();
        startAdminServer();
        MyTunesRssUtils.setCodewaveLogLevel(MyTunesRss.CONFIG.getCodewaveLogLevel());
        initializeQuicktimePlayer();
        checkHttpLiveStreamingSupport();
        logSystemInfo();
        prepareCacheDirs();
        Thread.setDefaultUncaughtExceptionHandler(new MyTunesRssUncaughtHandler(false));
        validateWrapperStartSystemProperty();
        processSanityChecks();
        startQuartzScheduler();
        initializeCaches();
        StatisticsEventManager.getInstance().addListener(new StatisticsDatabaseWriter());
        initializeDatabase();
        MyTunesRssJobUtils.scheduleStatisticEventsJob();
        MyTunesRssJobUtils.scheduleDatabaseJob();
        if (MyTunesRss.CONFIG.getPort() > 0) {
            startWebserver();
        }
        MyTunesRss.EXECUTOR_SERVICE.scheduleExternalAddressUpdate(); // must only be scheduled once
        MyTunesRss.EXECUTOR_SERVICE.scheduleUpdateCheck(); // must only be scheduled once
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

    private static void prepareCacheDirs() throws IOException {
        File tempDir = new File(MyTunesRssUtils.getCacheDataPath(), CACHEDIR_TEMP);
        File transcoderDir = new File(MyTunesRssUtils.getCacheDataPath(), CACHEDIR_TRANSCODER);
        File httpLiveStreamingDir = new File(MyTunesRssUtils.getCacheDataPath(), CACHEDIR_HTTP_LIVE_STREAMING);

        FileUtils.deleteQuietly(tempDir);
        FileUtils.deleteQuietly(transcoderDir);
        FileUtils.deleteQuietly(httpLiveStreamingDir);

        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        if (!transcoderDir.exists()) {
            transcoderDir.mkdirs();
        }
        if (!httpLiveStreamingDir.exists()) {
            httpLiveStreamingDir.mkdirs();
        }

    }

    private static void prepareUpdateUrl() {
        try {
            UPDATE_URL = new URL("http://www.codewave.de/download/versions/mytunesrss.xml");
        } catch (MalformedURLException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not create update external.", e);
            }
        }
    }

    private static void loadSystemProperties() {
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
    }

    private static void prepareLogging() {
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
        AppenderSkeleton appender = new AppenderSkeleton() {
            @Override
            protected void append(LoggingEvent event) {
                MyTunesRss.LOG_BUFFER.offer(new IndexedLoggingEvent(event));
                if (MyTunesRss.LOG_BUFFER.size() > 10000) { // limit backlog
                    MyTunesRss.LOG_BUFFER.poll();
                }
            }

            @Override
            public boolean requiresLayout() {
                return false;
            }

            @Override
            public void close() {
                MyTunesRss.LOG_BUFFER.clear();
            }
        };
        org.apache.log4j.Logger.getRootLogger().addAppender(appender);
        org.apache.log4j.Logger.getLogger("de.codewave").addAppender(appender);
    }

    private static void createDigests() {
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
    }

    private static void copyOldPrefsAndCache() {
        try {
            File cacheDataPath = new File(MyTunesRssUtils.getCacheDataPath());
            File prefsDataPath = new File(MyTunesRssUtils.getPreferencesDataPath());
            String[] prefsDataPathContents = prefsDataPath.list();
            if (prefsDataPathContents == null || prefsDataPathContents.length == 0) {
                for (String prevVersionAppIdentifier : APPLICATION_IDENTIFIER_PREV_VERSIONS) {
                    File oldPrefsDir = new File(PrefsUtils.getPreferencesDataPath(prevVersionAppIdentifier));
                    if (oldPrefsDir.isDirectory() && oldPrefsDir.list().length > 0) {
                        FileUtils.copyDirectory(oldPrefsDir, prefsDataPath);
                        File oldCacheDir = new File(PrefsUtils.getCacheDataPath(prevVersionAppIdentifier));
                        if (oldCacheDir.isDirectory() && oldCacheDir.list().length > 0) {
                            FileUtils.copyDirectory(oldCacheDir, cacheDataPath);
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not copy old preferences/caches.");
            }
        }
    }

    private static void initializeDatabase() throws IOException, SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        while (true) {
            registerDatabaseDriver();
            InitializeDatabaseCallable callable = new InitializeDatabaseCallable();
            callable.call();
            if (callable.getException() != null) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseInitError"));
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
                MyTunesRssUtils.showErrorMessage(MessageFormat.format(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseVersionMismatch"), MyTunesRss.VERSION, callable.getDatabaseVersion().toString()));
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
    }

    private static void initializeCaches() throws IOException {
        STREAMING_CACHE = new FileCache(APPLICATION_IDENTIFIER + "_Streaming", 10000, CONFIG.getStreamingCacheMaxFiles());
        TEMP_CACHE = new FileCache(APPLICATION_IDENTIFIER + "_Temp", 10000, 10000); // TODO max size config?
        HTTP_LIVE_STREAMING_CACHE = new ExpiringCache(APPLICATION_IDENTIFIER + "_HttpLiveStreaming", 10000, 10000); // TODO max size config?
    }

    private static void startQuartzScheduler() throws SchedulerException {
        QUARTZ_SCHEDULER = new StdSchedulerFactory().getScheduler();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting quartz scheduler.");
        }
        QUARTZ_SCHEDULER.start();
    }

    private static void processSanityChecks() {
        if (MyTunesRssUtils.isOtherInstanceRunning(3000)) {
            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.otherInstanceRunning"));
            MyTunesRssUtils.shutdownGracefully();
        }
        if (new Version(CONFIG.getVersion()).compareTo(new Version(VERSION)) > 0) {
            MyTunesRssUtils.showErrorMessage(MessageFormat.format(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.configVersionMismatch"), VERSION, CONFIG.getVersion()));
            MyTunesRssUtils.shutdownGracefully();
        }
        RegistrationFeedback feedback = MyTunesRssUtils.getRegistrationFeedback(Locale.getDefault());
        if (feedback != null && feedback.getMessage() != null) {
            MyTunesRssUtils.showErrorMessage(feedback.getMessage());
        }
    }

    private static void validateWrapperStartSystemProperty() {
        if (System.getProperty("de.codewave.mytunesrss") == null) {
            String type = "generic";
            if (SystemUtils.IS_OS_WINDOWS) {
                type = "windows";
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                type = "osx";
            }
            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.missingSystemProperty." + type));
        }
    }

    private static void logSystemInfo() throws IOException {
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
    }

    private static void handleRegistration() throws IOException {
        File license = null;
        if (COMMAND_LINE_ARGS.containsKey("license")) {
            license = new File(COMMAND_LINE_ARGS.get("license")[0]);
            if (!license.isFile()) {
                LOGGER.error("License file \"" + license.getAbsolutePath() + "\" specified on command line does not exist.");
                license = null;
            } else {
                LOGGER.info("Using license file \"" + license.getAbsolutePath() + "\" specified on command line.");
            }
        }
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
    }

    private static void loadConfig() {
        CONFIG = new MyTunesRssConfig();
        MyTunesRss.CONFIG.load();
    }

    private static void readVersion() {
        VERSION = MavenUtils.getVersion("de.codewave.mytunesrss", "runtime");
        if (StringUtils.isEmpty(VERSION)) {
            VERSION = System.getProperty("MyTunesRSS.version", "0.0.0");
        }
    }

    private static void processArguments(String[] args) {
        ORIGINAL_CMD_ARGS = args;
        Map<String, String[]> arguments = ProgramUtils.getCommandLineArguments(args);
        if (arguments != null) {
            COMMAND_LINE_ARGS.putAll(arguments);
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                MyTunesRssUtils.onShutdown();
            }
        }));
    }

    public static boolean startAdminServer() {
        try {
            int adminPort = MyTunesRss.CONFIG.getAdminPort();
            if (COMMAND_LINE_ARGS.get(CMD_ADMIN_PORT) != null) {
                try {
                    adminPort = Integer.parseInt(COMMAND_LINE_ARGS.get(CMD_ADMIN_PORT).toString());
                } catch (NumberFormatException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Invalid admin port " + COMMAND_LINE_ARGS.get(CMD_ADMIN_PORT) + " specified on commmand line.");
                    }
                }
            }
            if (adminPort > 0) {
                ADMIN_SERVER = new Server(adminPort);
            } else {
                ADMIN_SERVER = new Server(0);
            }
            WebAppContext adminContext = new WebAppContext("webapps/ADMIN", "/");
            adminContext.setServerClasses(new String[]{"-org.mortbay.jetty.plus.jaas.", "org.mortbay.jetty."});
            ADMIN_SERVER.setHandler(adminContext);
            ADMIN_SERVER.start();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Started admin server on port " + ADMIN_SERVER.getConnectors()[0].getLocalPort() + ".");
            }

            DesktopWrapper desktopWrapper = DesktopWrapperFactory.createDesktopWrapper();
            if (!HEADLESS) {
                new Thread() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, MyTunesRssUtils.getBundleString(Locale.getDefault(), "info.adminServerPortInfo", String.valueOf(ADMIN_SERVER.getConnectors()[0].getLocalPort())), MyTunesRssUtils.getBundleString(Locale.getDefault(), "info.title"), JOptionPane.INFORMATION_MESSAGE);
                    }
                }.start();
                if (desktopWrapper.isSupported() && !COMMAND_LINE_ARGS.containsKey(CMD_NO_BROWSER) && CONFIG.isStartAdminBrowser()) {
                    desktopWrapper.openBrowser(new URI("http://127.0.0.1:" + ADMIN_SERVER.getConnectors()[0].getLocalPort()));
                }
            } else {
                System.out.println("Started admin server on port " + ADMIN_SERVER.getConnectors()[0].getLocalPort());
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could start admin server.", e);
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

    private static void checkHttpLiveStreamingSupport() {
        try {
            File libDir = new File(MyTunesRssUtils.getPreferencesDataPath(), "lib");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Checking native libs for http live streaming support: \"" + libDir.getAbsolutePath() + "\".");
            }
            HTTP_LIVE_STREAMING_AVAILABLE = libDir.isDirectory() && HttpLiveStreamingSegmenter.isAvailable(libDir);
            if (!HTTP_LIVE_STREAMING_AVAILABLE) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Checking native libs for http live streaming support: \"" + MyTunesRssUtils.getNativeLibPath().getAbsolutePath() + "\".");
                }
                HTTP_LIVE_STREAMING_AVAILABLE = HttpLiveStreamingSegmenter.isAvailable(MyTunesRssUtils.getNativeLibPath());
            }
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not get prefs data path, assuming no http live streaming available.");
            }
            HTTP_LIVE_STREAMING_AVAILABLE = false;
        } finally {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Http live streaming status is \"" + HTTP_LIVE_STREAMING_AVAILABLE + "\".");
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
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseDriverNotFound",
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
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseDriverNotFound",
                        driverClassName,
                        libDir.getAbsolutePath()));
                MyTunesRssUtils.shutdownGracefully();
            }
        }
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
            MyTunesRss.EXECUTOR_SERVICE.scheduleMyTunesRssComUpdate();
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
            MyTunesRss.EXECUTOR_SERVICE.cancelMyTunesRssComUpdate();
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.SERVER_STOPPED));
        }
    }

}
