/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.camel.mp4.Mp4Parser;
import de.codewave.mytunesrss.bonjour.BonjourServiceListener;
import de.codewave.mytunesrss.config.MyTunesRssConfig;
import de.codewave.mytunesrss.config.RouterConfig;
import de.codewave.mytunesrss.datastore.DatabaseBackup;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingCacheItem;
import de.codewave.mytunesrss.job.MyTunesRssJobUtils;
import de.codewave.mytunesrss.network.MulticastService;
import de.codewave.mytunesrss.server.WebServer;
import de.codewave.mytunesrss.statistics.StatisticsDatabaseWriter;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.mytunesrss.task.DeleteDatabaseFilesCallable;
import de.codewave.mytunesrss.task.InitializeDatabaseCallable;
import de.codewave.mytunesrss.task.MessageOfTheDayRunnable;
import de.codewave.mytunesrss.vlc.VlcPlayer;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.ProgramUtils;
import de.codewave.utils.Version;
import de.codewave.utils.cache.ExpiringCache;
import de.codewave.utils.io.FileCache;
import de.codewave.utils.maven.MavenUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.net.ServerSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * de.codewave.mytunesrss.MyTunesRss
 */
public class MyTunesRss {
    // Specify admin server host on command line (e.g. -adminHost 192.168.0.3)
    public static final String CMD_ADMIN_HOST = "adminHost";

    // Specify admin server port on command line (e.g. -adminPort 9090)
    public static final String CMD_ADMIN_PORT = "adminPort";

    // Location of the cache data path (e.g. -cacheDataPath /var/mytunesrss/cache)
    public static final String CMD_CACHE_PATH = "cacheDataPath";

    // Location of the preferfences data path (e.g. -prefDataPath /var/mytunesrss/prefs)
    public static final String CMD_PREFS_PATH = "prefsDataPath";

    // Headless mode (no GUI elements)
    public static final String CMD_HEADLESS = "headless";

    // Alternative log4j configuration location (e.g. -logConfig ../etc/log4j-develop.xml)
    public static final String CMD_LOGCONFIG = "logConfig";

    // Shutdown port, send "SHUTDOWN" to this port to shutdown MyTunesRSS (e.g. -shutdownPort 12345)
    public static final String CMD_SHUTDOWN_PORT = "shutdownPort";

    // Send a shutdown request to the specified port (e.g. -shutdown 12345)
    public static final String CMD_SHUTDOWN = "shutdown";

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
    public static final String UPDATE_URL = "http://www.codewave.de/download/versions/mytunesrss.xml";
    public static MyTunesRssDataStore STORE = new MyTunesRssDataStore();
    public static MyTunesRssConfig CONFIG;
    public static WebServer WEBSERVER = new WebServer();
    public static MessageDigest SHA1_DIGEST;
    public static MessageDigest MD5_DIGEST;
    public static MyTunesRssRegistration REGISTRATION = new MyTunesRssRegistration();
    public static final String THREAD_PREFIX = "MyTunesRSS: ";
    public static FileCache STREAMING_CACHE;
    public static FileCache TEMP_CACHE;
    public static ExpiringCache<HttpLiveStreamingCacheItem> HTTP_LIVE_STREAMING_CACHE;
    public static Scheduler QUARTZ_SCHEDULER;
    public static MailSender MAILER = new MailSender();
    public static AdminNotifier ADMIN_NOTIFY = new AdminNotifier();
    public static BonjourServiceListener RAOP_LISTENER = new BonjourServiceListener();
    public static BonjourServiceListener AIRPLAY_LISTENER = new BonjourServiceListener();
    public static VlcPlayer VLC_PLAYER = new VlcPlayer(RAOP_LISTENER, AIRPLAY_LISTENER);
    public static LuceneTrackService LUCENE_TRACK_SERVICE = new LuceneTrackService();
    public static String[] ORIGINAL_CMD_ARGS;
    public static MyTunesRssExecutorService EXECUTOR_SERVICE = new MyTunesRssExecutorService();
    public static Server ADMIN_SERVER;
    public static final AtomicBoolean UNHANDLED_EXCEPTION = new AtomicBoolean(false);
    public static ResourceBundleManager RESOURCE_BUNDLE_MANAGER = new ResourceBundleManager(MyTunesRss.class.getClassLoader());
    public static BlockingQueue<IndexedLoggingEvent> LOG_BUFFER = new LinkedBlockingQueue<IndexedLoggingEvent>();
    public static final Thread.UncaughtExceptionHandler UNCAUGHT_HANDLER = new MyTunesRssUncaughtHandler();
    public static MyTunesRssForm FORM;
    public static MyTunesRssEvent LAST_DATABASE_EVENT;
    public static MessageOfTheDayRunnable MESSAGE_OF_THE_DAY = new MessageOfTheDayRunnable();
    public static RouterConfig ROUTER_CONFIG = new RouterConfig();
    public static final AtomicBoolean SHUTDOWN_IN_PROGRESS = new AtomicBoolean();
    public static String CACHE_DATA_PATH;
    public static String PREFERENCES_DATA_PATH;
    public static final Mp4Parser MP4_PARSER = new Mp4Parser();
    public static boolean RUN_DATABASE_REFRESH_ON_STARTUP = false;
    public static final Set<Process> SPAWNED_PROCESSES = new HashSet<Process>();

    public static void main(final String[] args) throws Exception {
        processArguments(args);
        if (COMMAND_LINE_ARGS.get(CMD_SHUTDOWN) != null && COMMAND_LINE_ARGS.get(CMD_SHUTDOWN).length > 0) {
            try {
                Integer port = Integer.parseInt(COMMAND_LINE_ARGS.get(CMD_SHUTDOWN)[0]);
                Socket socket = new Socket(InetAddress.getByName(null), port);
                socket.getOutputStream().write("SHUTDOWN".getBytes("US-ASCII"));
                socket.getOutputStream().flush();
                try {
                    socket.getInputStream().read();
                } catch (IOException e) {
                    // this is okay, the server has shutdown
                }
            } catch (NumberFormatException e) {
                System.err.println("Illegal shutdown port \"" + COMMAND_LINE_ARGS.get(CMD_SHUTDOWN)[0] + "\" specified.");
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Could not shutdown MyTunesRSS on port \"" + COMMAND_LINE_ARGS.get(CMD_SHUTDOWN)[0] + "\": " + e.getMessage());
                System.exit(1);
            }
            System.exit(0);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // try to kill all still running processes
                LOGGER.info("Trying to kill " + SPAWNED_PROCESSES.size() + " previously spawned processes.");
                for (Process process : SPAWNED_PROCESSES) {
                    process.destroy();
                }
                // try to do the best to shutdown the store in a clean way to keep H2 databases intact
                LOGGER.info("Running shutdown hook.");
                if (STORE != null && STORE.isInitialized()) {
                    LOGGER.info("Destroying still initialized store.");
                    STORE.destroy();
                }
            }
        });
        JmDNS bonjour = JmDNS.create();
        bonjour.addServiceListener("_raop._tcp.local.", RAOP_LISTENER);
        bonjour.addServiceListener("_airplay._tcp.local.", AIRPLAY_LISTENER);
        Thread.setDefaultUncaughtExceptionHandler(UNCAUGHT_HANDLER);
        CACHE_DATA_PATH = getCacheDataPath();
        PREFERENCES_DATA_PATH = getPreferencesDataPath();
        copyOldPrefsAndCache();
        createMissingPrefDirs();
        createDigests();
        prepareLogging();
        LOGGER.info("Command line: " + StringUtils.join(args, " "));
        WEBSERVER = new WebServer();
        MAILER = new MailSender();
        ADMIN_NOTIFY = new AdminNotifier();
        loadSystemProperties();
        readVersion();
        loadConfig();
        handleRegistration();
        MyTunesRssUtils.setCodewaveLogLevel(CONFIG.getCodewaveLogLevel());
        processSanityChecks();
        if (!MyTunesRssUtils.isHeadless()) {
            initMainWindow();
        }
        logSystemInfo();
        prepareCacheDirs();
        validateWrapperStartSystemProperty();
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            startQuartzScheduler();
        }
        initializeCaches();

        if (!SHUTDOWN_IN_PROGRESS.get()) {
            EXECUTOR_SERVICE.scheduleExternalAddressUpdate(); // must only be scheduled once
            EXECUTOR_SERVICE.scheduleUpdateCheck(); // must only be scheduled once
            EXECUTOR_SERVICE.scheduleWithFixedDelay(MESSAGE_OF_THE_DAY, 0, 900, TimeUnit.SECONDS); // refresh every 15 minutes
        }
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            initializeDatabase();
        }
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            StatisticsEventManager.getInstance().addListener(new StatisticsDatabaseWriter());
            EXECUTOR_SERVICE.scheduleWithFixedDelay(new MaintenanceRunnable(), 0, 3600, TimeUnit.SECONDS);
        }
        if (!SHUTDOWN_IN_PROGRESS.get() && COMMAND_LINE_ARGS.get(CMD_SHUTDOWN_PORT) != null && COMMAND_LINE_ARGS.get(CMD_SHUTDOWN_PORT).length > 0) {
            try {
                int port = Integer.parseInt(COMMAND_LINE_ARGS.get(CMD_SHUTDOWN_PORT)[0]);
                startShutdownListener(port);
            } catch (NumberFormatException e) {
                MyTunesRssUtils.showErrorMessage("Illegal shutdown port \"" + COMMAND_LINE_ARGS.get(CMD_SHUTDOWN_PORT)[0] + "\" specified.");
            }
        }
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            if (!startAdminServer(getAdminHostFromConfigOrCommandLine(), getAdminPortFromConfigOrCommandLine())) {
                MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.adminStartWithPortFailed", getAdminPortFromConfigOrCommandLine()));
                if (!startAdminServer(null, 0)) {
                    MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.adminStartFailed"));
                }
            }
        }
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            MyTunesRssJobUtils.scheduleStatisticEventsJob();
            MyTunesRssJobUtils.scheduleDatabaseJob();
            MyTunesRss.VLC_PLAYER.init();
        }
        startWebserver();
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            if (RUN_DATABASE_REFRESH_ON_STARTUP) {
                RUN_DATABASE_REFRESH_ON_STARTUP = false;
                MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(MyTunesRss.CONFIG.getDatasources(), true);
            }
            while (true) {
                try {
                    Thread.sleep(3600000); // sleep one hour
                } catch (InterruptedException e) {
                    LOGGER.debug("Main thread was interrupted.", e);
                    MyTunesRssUtils.shutdownGracefully();
                }
            }
        }
    }

    private static void startShutdownListener(final int port) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 0, InetAddress.getByName(null));
                    LOGGER.info("Started shutdown listener on port " + port + ".");
                    MyTunesRssUtils.showErrorMessage("Started shutdown listener on port " + port + ".");
                    while (true) {
                        Socket socket = null;
                        try {
                            socket = serverSocket.accept();
                            InputStream inputStream = socket.getInputStream();
                            byte[] buffer = new byte[] {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
                            while (!"SHUTDOWN".equals(new String(buffer, "US-ASCII"))) {
                                System.arraycopy(buffer, 1, buffer, 0, 7);
                                int nextByte = inputStream.read();
                                if (nextByte == -1) {
                                    break;
                                } else {
                                    buffer[7] = (byte)nextByte;
                                }
                            }
                            if ("SHUTDOWN".equals(new String(buffer, "US-ASCII"))) {
                                LOGGER.warn("Received shutdown signal on port " + port + ".");
                                MyTunesRssUtils.shutdownGracefully();
                            }
                        } catch (IOException e) {
                            LOGGER.error("Shutdown listener communication error.", e);
                        } finally {
                            if (socket != null) {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    LOGGER.error("Could not close shutdown listener communication socket.", e);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Could not start shutdown listener.", e);
                    MyTunesRssUtils.showErrorMessage("Could not start shutdown listener: " + e.getMessage());
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("MyTunesRSS Shutdown Listener");
        thread.start();
    }

    private static void initMainWindow() throws AWTException {
        FORM = new MyTunesRssForm();
    }

    private static void createMissingPrefDirs() throws IOException {
        for (String dir : new String[]{"lib", "themes", "languages", "flashplayer"}) {
            File file = new File(MyTunesRss.PREFERENCES_DATA_PATH, dir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
    }

    private static void prepareCacheDirs() throws IOException {
        File tempDir = new File(MyTunesRss.CACHE_DATA_PATH, CACHEDIR_TEMP);
        File transcoderDir = new File(MyTunesRss.CACHE_DATA_PATH, CACHEDIR_TRANSCODER);
        File httpLiveStreamingDir = new File(MyTunesRss.CACHE_DATA_PATH, CACHEDIR_HTTP_LIVE_STREAMING);

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

    private static void loadSystemProperties() {
        try {
            File file = new File(MyTunesRss.PREFERENCES_DATA_PATH + "/system.properties");
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
        System.setProperty("MyTunesRSS.logDir", MyTunesRss.CACHE_DATA_PATH);
        try {
            for (Iterator<File> iter =
                         (Iterator<File>) FileUtils.iterateFiles(new File(MyTunesRss.CACHE_DATA_PATH),
                                 new String[]{"log"},
                                 false); iter.hasNext(); ) {
                iter.next().delete();
            }
        } catch (Exception e) {
            // ignore exceptions when deleting log files
        }
        if (COMMAND_LINE_ARGS.get(CMD_LOGCONFIG) != null && COMMAND_LINE_ARGS.get(CMD_LOGCONFIG).length == 1 && COMMAND_LINE_ARGS.get(CMD_LOGCONFIG)[0] != null) {
            DOMConfigurator.configure(COMMAND_LINE_ARGS.get(CMD_LOGCONFIG)[0]);
        } else {
            DOMConfigurator.configure(MyTunesRss.class.getResource("/mytunesrss-log4j.xml"));
        }
        AppenderSkeleton appender = new AppenderSkeleton() {
            @Override
            protected void append(LoggingEvent event) {
                LOG_BUFFER.offer(new IndexedLoggingEvent(event));
                if (LOG_BUFFER.size() > 10000) { // limit backlog
                    LOG_BUFFER.poll();
                }
            }

            public boolean requiresLayout() {
                return false;
            }

            public void close() {
                LOG_BUFFER.clear();
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
            File cacheDataPath = new File(MyTunesRss.CACHE_DATA_PATH);
            File prefsDataPath = new File(MyTunesRss.PREFERENCES_DATA_PATH);
            String[] prefsDataPathContents = prefsDataPath.list();
            if (prefsDataPathContents == null || prefsDataPathContents.length == 0) {
                for (String prevVersionAppIdentifier : APPLICATION_IDENTIFIER_PREV_VERSIONS) {
                    File oldPrefsDir = new File(PrefsUtils.getPreferencesDataPathNoCreate(prevVersionAppIdentifier));
                    String[] oldPrefsDirFileNames = oldPrefsDir.list();
                    if (oldPrefsDir.isDirectory() && oldPrefsDirFileNames != null && oldPrefsDirFileNames.length > 0) {
                        FileUtils.copyDirectory(oldPrefsDir, prefsDataPath);
                        File oldCacheDir = new File(PrefsUtils.getCacheDataPathNoCreate(prevVersionAppIdentifier));
                        String[] oldCacheDirFileNames = oldCacheDir.list();
                        if (oldCacheDir.isDirectory() && oldCacheDirFileNames != null && oldCacheDirFileNames.length > 0) {
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
        boolean backupAfterSuccessfulInit = true;
        while (true) {
            registerDatabaseDriver();
            InitializeDatabaseCallable callable = new InitializeDatabaseCallable();
            callable.call();
            if (callable.getException() != null) {
                if (!MyTunesRssUtils.isHeadless() && CONFIG.isDefaultDatabase()) {
                    List<DatabaseBackup> backups = MyTunesRssUtils.findDatabaseBackups();
                    if (!backups.isEmpty()) {
                        backups.add(DatabaseBackup.NO_BACKUP);
                        DatabaseBackup backup = (DatabaseBackup) JOptionPane.showInputDialog(
                                null,
                                MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseInitErrorRestore"),
                                MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.title"),
                                JOptionPane.ERROR_MESSAGE,
                                null,
                                backups.toArray(new Object[backups.size()]),
                                null
                        );
                        if (backup != null) {
                            if (backup == DatabaseBackup.NO_BACKUP) {
                                recreateDefaultDatabase();
                                continue; // retry
                            } else {
                                try {
                                    MyTunesRss.STORE.destroy();
                                    MyTunesRssUtils.restoreDatabaseBackup(backup);
                                    backupAfterSuccessfulInit = false;
                                    LOGGER.info("Starting retry.");
                                    continue; // retry
                                } catch (IOException e) {
                                    LOGGER.error("Could not restore database backup.");
                                }
                            }
                        }
                    } else {
                        int result = JOptionPane.showConfirmDialog(null, MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseInitErrorReset"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.title"), JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            recreateDefaultDatabase();
                            continue;
                        }
                    }
                } else {
                    MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseInitError"));
                }
                MyTunesRssUtils.shutdownGracefully();
            }
            if (callable.getDatabaseVersion().compareTo(new Version(VERSION)) > 0) {
                if (!MyTunesRssUtils.isHeadless() && CONFIG.isDefaultDatabase()) {
                    int result = JOptionPane.showConfirmDialog(null, MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseVersionMismatchReset", VERSION, callable.getDatabaseVersion().toString()), MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.title"), JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        recreateDefaultDatabase();
                        continue; // retry
                    }
                } else {
                    MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseVersionMismatch", VERSION, callable.getDatabaseVersion().toString()));
                }
                MyTunesRssUtils.shutdownGracefully();
            }

            if (backupAfterSuccessfulInit && MyTunesRss.CONFIG.isBackupDatabaseAfterInit() && MyTunesRss.CONFIG.isDefaultDatabase()) {
                MyTunesRssUtils.backupDatabase();
            }
            MyTunesRssUtils.removeAllButLatestDatabaseBackups(MyTunesRss.CONFIG.getNumberKeepDatabaseBackups());
            break; // ok, continue with main flow
        }
    }

    private static void recreateDefaultDatabase() throws IOException {
        LOGGER.info("Recreating default database.");
        CONFIG.setDefaultDatabaseSettings();
        try {
            STORE.destroy();
            new DeleteDatabaseFilesCallable().call();
            LOGGER.info("Starting retry.");
        } catch (IOException e) {
            LOGGER.error("Could not delete database files.");
            CONFIG.setDeleteDatabaseOnExit(true);
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
        if (MyTunesRssUtils.lockInstance(3000)) {
            MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.otherInstanceRunning"));
            MyTunesRssUtils.shutdownGracefully();
        }
        if (new Version(CONFIG.getVersion()).compareTo(new Version(VERSION)) > 0) {
            MyTunesRssUtils.showErrorMessageWithDialog(MessageFormat.format(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.configVersionMismatch"), VERSION, CONFIG.getVersion()));
            MyTunesRssUtils.shutdownGracefully();
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
            MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.missingSystemProperty." + type));
        }
    }

    private static void logSystemInfo() throws IOException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Operating system: " + SystemUtils.OS_NAME + ", " + SystemUtils.OS_VERSION + ", " + SystemUtils.OS_ARCH);
            LOGGER.info("Java: " + SystemUtils.JAVA_VERSION + "(" + SystemUtils.JAVA_HOME + ")");
            LOGGER.info("Maximum heap size: " + MyTunesRssUtils.getMemorySizeForDisplay(Runtime.getRuntime().maxMemory()));
            LOGGER.info("Application version: " + VERSION);
            LOGGER.info("Cache data path: " + MyTunesRss.CACHE_DATA_PATH);
            LOGGER.info("Preferences data path: " + MyTunesRss.PREFERENCES_DATA_PATH);
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
            MyTunesRssConfig configFromFile = CONFIG;
            CONFIG = new MyTunesRssConfig();
            CONFIG.loadFromContext(REGISTRATION.getSettings());
            if (configFromFile.getPathInfoKey() != null) {
                CONFIG.setPathInfoKey(configFromFile.getPathInfoKey());
            }
        }
    }

    private static void loadConfig() {
        CONFIG = new MyTunesRssConfig();
        CONFIG.load();
    }

    private static void readVersion() throws IOException {
        LOGGER.info("Trying to get version from runtime pom.");
        VERSION = MavenUtils.getVersion("de.codewave.mytunesrss", "runtime");
        if (StringUtils.isEmpty(VERSION)) {
            String versionFileURI = System.getProperty("MyTunesRSS.versionFileURI");
            if (versionFileURI != null) {
                LOGGER.info("Trying to get version from \"" + versionFileURI + "\".");
                InputStream is = new URL(versionFileURI).openStream();
                try {
                    VERSION = IOUtils.toString(is).trim();
                } finally {
                    is.close();
                }
            } else {
                LOGGER.info("Trying to get version from system property \"MyTunesRSS.version\".");
                VERSION = System.getProperty("MyTunesRSS.version", "0.0.0");
            }
        }
        LOGGER.info("Got version \"" + VERSION + "\".");
    }

    private static void processArguments(String[] args) {
        ORIGINAL_CMD_ARGS = args;
        Map<String, String[]> arguments = ProgramUtils.getCommandLineArguments(args);
        if (arguments != null) {
            COMMAND_LINE_ARGS.putAll(arguments);
        }
    }

    public static boolean startAdminServer(String adminHost, int adminPort) {
        try {
            ADMIN_SERVER = new Server(new InetSocketAddress(StringUtils.defaultIfBlank(adminHost, "0.0.0.0"), adminPort));
            WebAppContext adminContext = new WebAppContext("webapps/ADMIN", "/");
            adminContext.setSystemClasses((String[]) ArrayUtils.add(adminContext.getSystemClasses(), "de.codewave."));
            File workDir = new File(MyTunesRss.CACHE_DATA_PATH + "/jetty-admin-work");
            if (workDir.exists()) {
                MyTunesRssUtils.deleteRecursivly(workDir);// at least try to delete the working directory before starting the server to dump outdated stuff
            }
            adminContext.setTempDirectory(workDir);
            ADMIN_SERVER.setHandler(adminContext);
            ADMIN_SERVER.start();
            ROUTER_CONFIG.addAdminPortMapping(adminPort);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could start admin server.", e);
            }
            if (FORM != null) {
                FORM.setAdminUrl(e);
            }
            return false;
        }
        int localPort = ADMIN_SERVER.getConnectors()[0].getLocalPort();
        if (FORM != null) {
            FORM.setAdminUrl(ADMIN_SERVER.getConnectors()[0].getHost(), localPort);
        }
        try {
            FileUtils.writeStringToFile(new File(MyTunesRss.CACHE_DATA_PATH, "adminport"), localPort + "\n");
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not write admin port to file.", e);
            }

        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Started admin server on port " + localPort + ".");
        }
        if (MyTunesRssUtils.isHeadless()) {
            System.out.println("Started admin server on port " + localPort);
        }
        return true;
    }

    private static String getAdminHostFromConfigOrCommandLine() {
        String adminHost = CONFIG.getAdminHost();
        if (COMMAND_LINE_ARGS.get(CMD_ADMIN_HOST) != null) {
            adminHost = COMMAND_LINE_ARGS.get(CMD_ADMIN_HOST)[0].toString();
        }
        return adminHost;
    }

    private static int getAdminPortFromConfigOrCommandLine() {
        int adminPort = CONFIG.getAdminPort();
        if (COMMAND_LINE_ARGS.get(CMD_ADMIN_PORT) != null) {
            try {
                adminPort = Integer.parseInt(COMMAND_LINE_ARGS.get(CMD_ADMIN_PORT)[0].toString());
            } catch (NumberFormatException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Invalid admin port " + COMMAND_LINE_ARGS.get(CMD_ADMIN_PORT) + " specified on commmand line.");
                }
            }
        }
        return adminPort;
    }

    public static boolean stopAdminServer() {
        try {
            ROUTER_CONFIG.deleteAdminPortMapping();
            ADMIN_SERVER.stop();
            ADMIN_SERVER.join();
            FileUtils.deleteQuietly(new File(MyTunesRss.CACHE_DATA_PATH, "adminport"));
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Cannot stop admin server.", e);
            }
            return false;
        }
        return true;
    }

    private static ClassLoader createExtraClassloader(File libDir) {
        try {
            Collection<File> files = libDir.isDirectory() ? FileUtils.listFiles(libDir, new String[]{"jar", "zip"}, false) : null;
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
        File libDir = new File(MyTunesRss.PREFERENCES_DATA_PATH + "/lib");
        ClassLoader classLoader = createExtraClassloader(libDir);
        String driverClassName = CONFIG.getDatabaseDriver();
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
                MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseDriverNotFound",
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
                MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseDriverNotFound",
                        driverClassName,
                        libDir.getAbsolutePath()));
                MyTunesRssUtils.shutdownGracefully();
            }
        }
    }

    public static Exception startWebserver() {
        try {
            WEBSERVER.start();
            if (WEBSERVER.isRunning()) {
                MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.SERVER_STARTED));
                if (FORM != null) {
                    FORM.setUserUrl(CONFIG.getPort());
                }
                EXECUTOR_SERVICE.scheduleMyTunesRssComUpdate();
                if (CONFIG.isAvailableOnLocalNet()) {
                    MulticastService.startListener();
                }
            } else if (FORM != null) {
                FORM.setUserUrl(-1);
            }
        } catch (Exception e) {
            if (FORM != null) {
                FORM.setUserUrl(e);
            } else {
                LOGGER.error("Could not start MyTunesRSS server,", e);
                MyTunesRssUtils.showErrorMessage(e.getMessage());
            }
            return e;
        }
        return null;
    }

    public static void stopWebserver() {
        WEBSERVER.stop();
        if (!WEBSERVER.isRunning()) {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.SERVER_STOPPED));
            if (FORM != null) {
                FORM.setUserUrl(-1);
            }
            MulticastService.stopListener();
            EXECUTOR_SERVICE.cancelMyTunesRssComUpdate();
        }
    }

    private static String getCacheDataPath() throws IOException {
        if (MyTunesRss.COMMAND_LINE_ARGS.containsKey(MyTunesRss.CMD_CACHE_PATH)) {
            return MyTunesRss.COMMAND_LINE_ARGS.get(MyTunesRss.CMD_CACHE_PATH)[0];
        } else {
            return PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        }
    }

    private static String getPreferencesDataPath() throws IOException {
        if (MyTunesRss.COMMAND_LINE_ARGS.containsKey(MyTunesRss.CMD_PREFS_PATH)) {
            return MyTunesRss.COMMAND_LINE_ARGS.get(MyTunesRss.CMD_PREFS_PATH)[0];
        } else {
            return PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        }
    }
}
