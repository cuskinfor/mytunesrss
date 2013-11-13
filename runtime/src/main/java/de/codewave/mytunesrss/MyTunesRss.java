/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import com.sun.management.HotSpotDiagnosticMXBean;
import de.codewave.camel.mp4.Mp4Parser;
import de.codewave.mytunesrss.bonjour.BonjourServiceListener;
import de.codewave.mytunesrss.cache.FileSystemCache;
import de.codewave.mytunesrss.config.DatabaseType;
import de.codewave.mytunesrss.config.MyTunesRssConfig;
import de.codewave.mytunesrss.config.RouterConfig;
import de.codewave.mytunesrss.datastore.DatabaseBackup;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.job.MyTunesRssJobUtils;
import de.codewave.mytunesrss.lucene.AddLuceneTrack;
import de.codewave.mytunesrss.lucene.LuceneTrack;
import de.codewave.mytunesrss.lucene.LuceneTrackService;
import de.codewave.mytunesrss.network.MulticastService;
import de.codewave.mytunesrss.server.WebServer;
import de.codewave.mytunesrss.statistics.StatisticsDatabaseWriter;
import de.codewave.mytunesrss.statistics.StatisticsEventManager;
import de.codewave.mytunesrss.task.DeleteDatabaseFilesCallable;
import de.codewave.mytunesrss.task.InitializeDatabaseCallable;
import de.codewave.mytunesrss.task.MessageOfTheDayRunnable;
import de.codewave.mytunesrss.vlc.VlcPlayer;
import de.codewave.mytunesrss.vlc.VlcPlayerException;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.ProgramUtils;
import de.codewave.utils.Version;
import de.codewave.utils.maven.MavenUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;
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
import javax.management.MBeanServer;
import javax.net.ServerSocketFactory;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    // Reset the database to the default H2 database
    public static final String CMD_RESET_DB = "resetDatabase";

    // Cache directory names
    public static final String CACHEDIR_TEMP = "tmp";
    public static final String CACHEDIR_TRANSCODER = "transcoder";
    public static final String CACHEDIR_HTTP_LIVE_STREAMING = "http_live_streaming";

    public static final String APPLICATION_IDENTIFIER = "MyTunesRSS5";
    public static final String[] APPLICATION_IDENTIFIER_PREV_VERSIONS = new String[]{"MyTunesRSS4", "MyTunesRSS3"};
    public static final Map<String, String[]> COMMAND_LINE_ARGS = new HashMap<String, String[]>();
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRss.class);
    public static final String MYTUNESRSSCOM_URL = "http://mytunesrss.com";
    public static final String MYTUNESRSSCOM_TOOLS_URL = MYTUNESRSSCOM_URL + "/tools";
    public static final long FACTOR_GIB_TO_BYTE = 1024L * 1024L * 1024L;
    public static final long STARTUP_TIME = System.currentTimeMillis();
    private static final BlockingQueue<MessageWithParameters> IMPORTANT_ADMIN_MESSAGE = new ArrayBlockingQueue<MessageWithParameters>(10);
    public static String VERSION;
    public static final String UPDATE_URL = "http://www.codewave.de/download/versions/mytunesrss.xml";
    public static MyTunesRssDataStore STORE = new MyTunesRssDataStore();
    public static MyTunesRssConfig CONFIG;
    public static WebServer WEBSERVER = new WebServer();
    public static ThreadLocal<MessageDigest> SHA1_DIGEST = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not create SHA-1 digest.", e);
                }
            }
            remove();
            return null;
        }
    };
    public static ThreadLocal<MessageDigest> MD5_DIGEST = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not create MD5 digest.", e);
                }
            }
            remove();
            return null;
        }
    };
    public static MyTunesRssRegistration REGISTRATION = new MyTunesRssRegistration();
    public static final String THREAD_PREFIX = "MyTunesRSS: ";
    public static FileSystemCache TRANSCODER_CACHE;
    public static FileSystemCache TEMP_CACHE;
    public static FileSystemCache HTTP_LIVE_STREAMING_CACHE;
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
    public static AtomicReference<MyTunesRssEvent> LAST_DATABASE_EVENT = new AtomicReference<MyTunesRssEvent>();
    public static MessageOfTheDayRunnable MESSAGE_OF_THE_DAY = new MessageOfTheDayRunnable();
    public static RouterConfig ROUTER_CONFIG = new RouterConfig();
    public static final AtomicBoolean SHUTDOWN_IN_PROGRESS = new AtomicBoolean();
    public static String CACHE_DATA_PATH;
    public static String PREFERENCES_DATA_PATH;
    public static final Mp4Parser MP4_PARSER = new Mp4Parser();
    public static boolean RUN_DATABASE_REFRESH_ON_STARTUP = false;
    public static boolean REBUILD_LUCENE_INDEX_ON_STARTUP = false;
    public static final Set<Process> SPAWNED_PROCESSES = new HashSet<Process>();
    public static JmDNS BONJOUR;
    public static String HEAPDUMP_FILENAME;
    public static ClassLoader EXTRA_CLASSLOADER;
    public static File INTERNAL_MYSQL_SERVER_PATH;

    public static void main(final String[] args) throws Exception {
        processArguments(args);
        CACHE_DATA_PATH = getCacheDataPath();
        PREFERENCES_DATA_PATH = getPreferencesDataPath();
        INTERNAL_MYSQL_SERVER_PATH = new File(MyTunesRss.CACHE_DATA_PATH + "/mysqldb");
        // shutdown command
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
        // locking
        if (MyTunesRssUtils.lockInstance(3000)) {
            if (!MyTunesRssUtils.isHeadless()) {
                JOptionPane.showMessageDialog(null, MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.otherInstanceRunning"));
            } else {
                System.err.println(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.otherInstanceRunning"));
            }
            System.exit(0);
        }
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Running shutdown hook.");
                // try to kill all still running processes
                LOGGER.info("Trying to kill " + SPAWNED_PROCESSES.size() + " previously spawned processes.");
                for (Process process : SPAWNED_PROCESSES) {
                    process.destroy();
                }
                // try to do the best to shutdown the store in a clean way to keep H2 databases intact
                if (STORE != null && STORE.isInitialized()) {
                    LOGGER.info("Destroying still initialized store.");
                    STORE.destroy();
                }
                // try to kill internal mysql database
                if (CONFIG.getDatabaseType() == DatabaseType.mysqlinternal) {
                    LOGGER.info("Trying to shutdown internal mysql server.");
                    try {
                        Class clazz = Class.forName("com.mysql.management.driverlaunched.ServerLauncherSocketFactory", true, EXTRA_CLASSLOADER);
                        clazz.getMethod("shutdown", File.class, File.class).invoke(null, INTERNAL_MYSQL_SERVER_PATH, null);
                    } catch (Exception e) {
                        LOGGER.warn("Could not shutdown internal mysql server.", e);
                    }
                }

            }
        });
        Thread.setDefaultUncaughtExceptionHandler(UNCAUGHT_HANDLER);
        copyOldPrefsAndCache();
        createMissingPrefDirs();
        prepareLogging();
        LOGGER.info("Command line: " + StringUtils.join(args, " "));
        enableHeapDumpOnOutOfMemoryError();
        WEBSERVER = new WebServer();
        MAILER = new MailSender();
        ADMIN_NOTIFY = new AdminNotifier();
        EXTRA_CLASSLOADER = createExtraClassloader(new File(MyTunesRss.PREFERENCES_DATA_PATH + "/lib"));
        loadSystemProperties();
        readVersion();
        loadConfig();
        handleRegistration();
        MyTunesRssUtils.setCodewaveLogLevel(CONFIG.getCodewaveLogLevel());
        processSanityChecks();
        if (!MyTunesRssUtils.isHeadless()) {
            initMainWindow();
        } else if (!GraphicsEnvironment.isHeadless() && SystemUtils.IS_OS_MAC_OSX) {
            executeAppleHeadlessOnNonHeadlessSystem();
        }
        logSystemInfo();
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
            if (MyTunesRss.COMMAND_LINE_ARGS.containsKey(MyTunesRss.CMD_RESET_DB)) {
                LOGGER.info("Recreation of default database requested via command line option.");
                CONFIG.setDatabaseType(DatabaseType.h2);
                CONFIG.setDefaultDatabaseSettings();
            }
            initializeDatabase();
        }
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            EXECUTOR_SERVICE.scheduleImageGenerators();
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
            try {
                BONJOUR = JmDNS.create();
                BONJOUR.addServiceListener("_raop._tcp.local.", RAOP_LISTENER);
                BONJOUR.addServiceListener("_airplay._tcp.local.", AIRPLAY_LISTENER);
            } catch (IOException e) {
                LOGGER.warn("Could not add service listeners for RAOP and AIRPLAY.", e);
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
            MyTunesRss.EXECUTOR_SERVICE.execute(new Runnable() {
                public void run() {
                    try {
                        MyTunesRss.VLC_PLAYER.init();
                    } catch (VlcPlayerException e) {
                        LOGGER.warn("Could not start VLC-Player.", e);
                    }
                }
            });
        }
        startWebserver();
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            if (RUN_DATABASE_REFRESH_ON_STARTUP) {
                RUN_DATABASE_REFRESH_ON_STARTUP = false;
                if (REBUILD_LUCENE_INDEX_ON_STARTUP) {
                    REBUILD_LUCENE_INDEX_ON_STARTUP = false;
                    MyTunesRss.LUCENE_TRACK_SERVICE.deleteLuceneIndex();
                }
                MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(MyTunesRss.CONFIG.getDatasources(), true);
            }
        }
        if (!SHUTDOWN_IN_PROGRESS.get()) {
            if (REBUILD_LUCENE_INDEX_ON_STARTUP) {
                REBUILD_LUCENE_INDEX_ON_STARTUP = false;
                LOGGER.info("Recreating lucene index from scratch.");
                long start = System.currentTimeMillis();
                try {
                    MyTunesRss.LUCENE_TRACK_SERVICE.deleteLuceneIndex();
                    FindPlaylistTracksQuery query = new FindPlaylistTracksQuery(FindPlaylistTracksQuery.PSEUDO_ID_ALL_BY_ALBUM, SortOrder.KeepOrder);
                    query.setResultSetType(ResultSetType.TYPE_FORWARD_ONLY);
                    query.setFetchSize(1000);
                    DataStoreQuery.QueryResult<Track> trackQueryResult = MyTunesRss.STORE.executeQuery(query);
                    for (Track track = trackQueryResult.nextResult(); track != null; track = trackQueryResult.nextResult()) {
                        LuceneTrack luceneTrack = new AddLuceneTrack();
                        luceneTrack.setId(track.getId());
                        luceneTrack.setSourceId(track.getSourceId());
                        luceneTrack.setAlbum(track.getAlbum());
                        luceneTrack.setAlbumArtist(track.getAlbumArtist());
                        luceneTrack.setArtist(track.getArtist());
                        luceneTrack.setComment(track.getComment());
                        luceneTrack.setComposer(track.getComposer());
                        luceneTrack.setFilename(track.getFilename());
                        luceneTrack.setGenre(track.getGenre());
                        luceneTrack.setName(track.getName());
                        luceneTrack.setSeries(track.getSeries());
                        try {
                            MyTunesRss.LUCENE_TRACK_SERVICE.updateTrack(luceneTrack);
                        } catch (IOException e) {
                            LOGGER.error("Could not update lucene index for track \"" + track.getId() + "\".", e);
                        }
                    }
                    LOGGER.info("Finished recreating lucene index from scratch (duration = " + (System.currentTimeMillis() - start) + " milliseconds).");
                } catch (IOException e) {
                    LOGGER.error("Could not recreate lucene index.", e);
                } catch (SQLException e) {
                    LOGGER.error("Could not recreate lucene index.", e);
                }
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

    private static void executeAppleHeadlessOnNonHeadlessSystem() {
        LOGGER.debug("Trying to execute apple specific code for headless mode on non-headless system.");
        try {
            LOGGER.debug("Executing apple specific code for headless mode on non-headless system.");
            Class appleExtensionsClass = Class.forName("de.codewave.apple.AppleExtensions");
            Method activateMethod = appleExtensionsClass.getMethod("activate", EventListener.class);
            activateMethod.invoke(null, new AppleExtensionsEventListenerHeadlessMode());
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not activate apple extensions.", e);
            }
        }
    }

    private static void enableHeapDumpOnOutOfMemoryError() {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            File heapdumpFolder = new File(CACHE_DATA_PATH + "/heapdumps");
            if (!heapdumpFolder.isDirectory()) {
                heapdumpFolder.mkdirs();
            }
            FileUtils.cleanDirectory(heapdumpFolder);
            HEAPDUMP_FILENAME = heapdumpFolder.getAbsolutePath() + "/mytunesrss-" + runtimeMXBean.getName().split("@")[0] + ".hprof";
            hotSpotDiagnosticMXBean.setVMOption("HeapDumpPath", HEAPDUMP_FILENAME);
            hotSpotDiagnosticMXBean.setVMOption("HeapDumpOnOutOfMemoryError", "true");
        } catch (Throwable e) {
            // no exception here shall ever break MyTunesRSS
            LOGGER.warn("Could not completely enable heap dumps on OutOfMemory error.", e);
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

    private static void loadSystemProperties() {
        try {
            File file = new File(MyTunesRss.PREFERENCES_DATA_PATH + "/system.properties");
            if (file.isFile()) {
                Properties properties = new Properties();
                FileInputStream inStream = new FileInputStream(file);
                try {
                    properties.load(inStream);
                } finally {
                    IOUtils.closeQuietly(inStream);
                }
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
                        break;
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
            try {
                registerDatabaseDriver();
            } catch (Exception e) {
                if (!CONFIG.isDefaultDatabase()) {
                    int result = JOptionPane.showConfirmDialog(null, MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseInitErrorReset"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.title"), JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        recreateDefaultDatabase();
                        continue;
                    }
                } else {
                    MyTunesRssUtils.showErrorMessageWithDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseInitError"));
                    MyTunesRssUtils.shutdownGracefully();
                }
            }
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
                    if (!MyTunesRssUtils.isHeadless()) {
                        int result = JOptionPane.showConfirmDialog(null, MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.databaseInitErrorReset"), MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.title"), JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            recreateDefaultDatabase();
                            continue;
                        }
                    }
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
        CONFIG.setDatabaseType(DatabaseType.h2);
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
        TRANSCODER_CACHE = new FileSystemCache("Transcoder", new File(MyTunesRss.CACHE_DATA_PATH + "/" + MyTunesRss.CACHEDIR_TRANSCODER), CONFIG.getTranscodingCacheMaxGiB() * FACTOR_GIB_TO_BYTE, 60000L);
        TRANSCODER_CACHE.init();
        TEMP_CACHE = new FileSystemCache("Temp", new File(MyTunesRss.CACHE_DATA_PATH, MyTunesRss.CACHEDIR_TEMP), MyTunesRss.CONFIG.getTempMaxGiB() * FACTOR_GIB_TO_BYTE, 60000L);
        TEMP_CACHE.init();
        if (!TEMP_CACHE.clear()) {
            LOGGER.warn("Could not clean temporary cache dir.");
        }
        HTTP_LIVE_STREAMING_CACHE = new FileSystemCache("HttpLiveStreaming", new File(MyTunesRss.CACHE_DATA_PATH, MyTunesRss.CACHEDIR_HTTP_LIVE_STREAMING), MyTunesRss.CONFIG.getHttpLiveStreamCacheMaxGiB() * FACTOR_GIB_TO_BYTE, 60000L);
        HTTP_LIVE_STREAMING_CACHE.init();
    }

    private static void startQuartzScheduler() throws SchedulerException {
        QUARTZ_SCHEDULER = new StdSchedulerFactory().getScheduler();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Starting quartz scheduler.");
        }
        QUARTZ_SCHEDULER.start();
    }

    private static void processSanityChecks() {
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
            adminContext.setHandler(MyTunesRssUtils.createJettyAccessLogHandler("admin", MyTunesRss.CONFIG.getAdminAccessLogRetainDays(), MyTunesRss.CONFIG.isAdminAccessLogExtended(), MyTunesRss.CONFIG.getAccessLogTz())); // TODO config
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
                    LOGGER.info("Adding \"" + file.getAbsolutePath() + "\" to extra classpath.");
                    urls.add(file.toURL());
                }
                return new URLClassLoader(urls.toArray(new URL[urls.size()]), ClassLoader.getSystemClassLoader());
            }
        } catch (IOException e) {
            LOGGER.error("Could not create extra classloader.", e);
        }
        return null;
    }

    private static void registerDatabaseDriver() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        String driverClassName = CONFIG.getDatabaseDriver();
        LOGGER.info("Using database driver class \"" + driverClassName + "\".");
        if (EXTRA_CLASSLOADER != null) {
            try {
                final Class<Driver> driverClass = (Class<Driver>) Class.forName(driverClassName, true, EXTRA_CLASSLOADER);
                DriverManager.registerDriver(SystemUtils.isJavaVersionAtLeast(160) ? new Java6SqlDriver(driverClass) : new Java5SqlDriver(driverClass));
            } catch (SQLException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(null, e);
                }
            }
        } else {
            Class.forName(driverClassName);
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

    public static MessageWithParameters getImportantAdminMessage() {
        try {
            return IMPORTANT_ADMIN_MESSAGE.remove();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public static void addImportAdminMessage(String msg, Object... parameters) {
        MessageWithParameters messageWithParameters = new MessageWithParameters(msg, parameters);
        try {
            IMPORTANT_ADMIN_MESSAGE.offer(messageWithParameters, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // remove oldest message to add a new one
            IMPORTANT_ADMIN_MESSAGE.remove();
            try {
                IMPORTANT_ADMIN_MESSAGE.offer(messageWithParameters, 100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e1) {
                // ignore
            }
        }
    }

    public static class AppleExtensionsEventListenerHeadlessMode implements EventListener {
        public void handleQuit() {
            LOGGER.debug("Apple extension: handleQuit.");
            MyTunesRssUtils.shutdownGracefully();
        }

        public void handleReOpenApplication() {
            LOGGER.debug("Apple extension: handleReOpenApplication. Ignored in headless mode.");
        }
    }


}
