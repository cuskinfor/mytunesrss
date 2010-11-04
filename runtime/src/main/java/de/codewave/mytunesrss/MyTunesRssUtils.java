package de.codewave.mytunesrss;

import com.ibm.icu.text.Normalizer;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.RemoveOldTempPlaylistsStatement;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.mytunesrss.task.DeleteDatabaseFilesCallable;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggerRepository;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * de.codewave.mytunesrss.MyTunesRssUtils
 */
public class MyTunesRssUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUtils.class);

    public static byte[] getUtf8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    public static String getUtf8String(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    public static String getUtf8UrlEncoded(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    public static String getUtf8UrlDecoded(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        } else if (o1 == null || o2 == null) {
            return false;
        } else if (o1 instanceof byte[] && o2 instanceof byte[]) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        } else if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        } else if (o1 instanceof short[] && o2 instanceof short[]) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        } else if (o1 instanceof int[] && o2 instanceof int[]) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        } else if (o1 instanceof long[] && o2 instanceof long[]) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        } else if (o1 instanceof float[] && o2 instanceof float[]) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        } else if (o1 instanceof double[] && o2 instanceof double[]) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        } else if (o1 instanceof boolean[] && o2 instanceof boolean[]) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        } else if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        }
        return o1.equals(o2);
    }

    public static void showErrorMessage(String message) {
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(message);
        }
        System.err.println(message);
    }

    public static String getBundleString(Locale locale, String key, Object... parameters) {
        if (key == null) {
            return "";
        }
        ResourceBundle bundle = MyTunesRss.RESOURCE_BUNDLE_MANAGER.getBundle("de.codewave.mytunesrss.MyTunesRss", locale);
        if (parameters == null || parameters.length == 0) {
            return bundle.getString(key);
        }
        return MessageFormat.format(bundle.getString(key), parameters);
    }

    public static HttpClient createHttpClient() {
        HttpClient httpClient = new HttpClient();
        DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(1, true);
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setSoTimeout(10000);
        httpClient.setHttpConnectionManager(connectionManager);
        if (MyTunesRss.CONFIG.isProxyServer()) {
            HostConfiguration hostConfiguration = new HostConfiguration();
            hostConfiguration.setProxy(MyTunesRss.CONFIG.getProxyHost(), MyTunesRss.CONFIG.getProxyPort());
            httpClient.setHostConfiguration(hostConfiguration);
        }
        return httpClient;
    }

    public static void shutdownGracefully() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Shutting down gracefully.");
        }
        MyTunesRss.EXECUTOR_SERVICE.cancelDatabaseJob();
        if (MyTunesRss.WEBSERVER != null && MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.stopWebserver();
        }
        if (MyTunesRss.WEBSERVER == null || !MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.SERVER_RUNNING_TIMER.cancel();
            if (MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Database still updating... waiting for it to finish.");
                }
                while (MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // intentionally left blank
                    }
                }
            }
            if (MyTunesRss.STORE != null && MyTunesRss.STORE.isInitialized()) {
                DataStoreSession session = MyTunesRss.STORE.getTransaction();
                try {
                    LOGGER.debug("Removing old temporary playlists.");
                    session.executeStatement(new RemoveOldTempPlaylistsStatement());
                    session.commit();
                } catch (SQLException e) {
                    LOGGER.error("Could not remove old temporary playlists.", e);
                    try {
                        session.rollback();
                    } catch (SQLException e1) {
                        LOGGER.error("Could not rollback transaction.", e1);
                    }
                }
                try {
                    LOGGER.debug("Removing old statistic events.");
                    session.executeStatement(new RemoveOldEventsStatement());
                    session.commit();
                } catch (SQLException e) {
                    LOGGER.error("Could not remove old statistic events.", e);
                    try {
                        session.rollback();
                    } catch (SQLException e1) {
                        LOGGER.error("Could not rollback transaction.", e1);
                    }
                }
                LOGGER.debug("Destroying store.");
                MyTunesRss.STORE.destroy();
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Shutting down.");
        }
        if (MyTunesRss.QUARTZ_SCHEDULER != null) {
            try {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Shutting down quartz scheduler.");
                }
                MyTunesRss.QUARTZ_SCHEDULER.shutdown();
            } catch (SchedulerException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not shutdown quartz scheduler.", e);
                }
            }
        }
        if (MyTunesRss.CONFIG.isDefaultDatabase() && MyTunesRss.CONFIG.isDeleteDatabaseOnExit()) {
            try {
                new DeleteDatabaseFilesCallable().call();
            } catch (IOException e) {
                LOGGER.error("Could not delete default database files.");
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Very last log message before shutdown.");
        }
        System.exit(0);
    }

    public static void onShutdown() {
        if (MyTunesRss.STREAMING_CACHE != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cleaning up streaming cache.");
            }
            MyTunesRss.STREAMING_CACHE.clearCache();
        }
        if (MyTunesRss.TEMP_CACHE != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cleaning up temp cache.");
            }
            MyTunesRss.TEMP_CACHE.clearCache();
        }
        if (MyTunesRss.HTTP_LIVE_STREAMING_CACHE != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cleaning up http live streaming cache.");
            }
            MyTunesRss.HTTP_LIVE_STREAMING_CACHE.clearCache();
        }
    }

    private static final double KBYTE = 1024;
    private static final double MBYTE = 1024 * KBYTE;
    private static final double GBYTE = 1024 * MBYTE;
    private static final NumberFormat BYTE_STREAMED_FORMAT = new DecimalFormat("0");
    private static final NumberFormat KBYTE_STREAMED_FORMAT = new DecimalFormat("0");
    private static final DecimalFormat MBYTE_STREAMED_FORMAT = new DecimalFormat("0.##");
    private static final DecimalFormat GBYTE_STREAMED_FORMAT = new DecimalFormat("0.#");

    static {
        MBYTE_STREAMED_FORMAT.setDecimalSeparatorAlwaysShown(false);
        GBYTE_STREAMED_FORMAT.setDecimalSeparatorAlwaysShown(false);
    }

    public static String getMemorySizeForDisplay(long bytes) {
        if (bytes >= GBYTE) {
            return GBYTE_STREAMED_FORMAT.format(bytes / GBYTE) + " GB";
        } else if (bytes >= MBYTE) {
            return MBYTE_STREAMED_FORMAT.format(bytes / MBYTE) + " MB";
        } else if (bytes >= KBYTE) {
            return KBYTE_STREAMED_FORMAT.format(bytes / KBYTE) + " KB";
        }
        return BYTE_STREAMED_FORMAT.format(bytes) + " Byte";
    }

    public static boolean deleteRecursivly(File file) {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                if (!deleteRecursivly(subFile)) {
                    return false;
                }
            }
            file.delete();
        } else if (file.isFile()) {
            return file.delete();
        }
        return true;
    }

    public static SmartStatement createStatement(Connection connection, String name) throws SQLException {
        return createStatement(connection, name, Collections.<String, Boolean>emptyMap(), ResultSetType.TYPE_SCROLL_INSENSITIVE);
    }

    public static SmartStatement createStatement(Connection connection, String name, Map<String, Boolean> conditionals) throws SQLException {
        return createStatement(connection, name, conditionals, ResultSetType.TYPE_SCROLL_INSENSITIVE);
    }

    public static SmartStatement createStatement(Connection connection, String name, ResultSetType resultSetType) throws SQLException {
        return createStatement(connection, name, Collections.<String, Boolean>emptyMap(), ResultSetType.TYPE_SCROLL_INSENSITIVE);
    }

    public static SmartStatement createStatement(Connection connection, String name, final Map<String, Boolean> conditionals, ResultSetType resultSetType) throws SQLException {
        return MyTunesRss.STORE.getSmartStatementFactory().createStatement(connection, name, (Map<String, Boolean>) Proxy.newProxyInstance(MyTunesRss.class.getClassLoader(), new Class[]{Map.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("get".equals(method.getName()) && args.length == 1 && args[0] instanceof String) {
                    return conditionals.containsKey(args[0]) ? conditionals.get(args[0]) : Boolean.FALSE;
                } else {
                    return method.invoke(conditionals, args);
                }
            }
        }), resultSetType);
    }

    public static void executeDatabaseUpdate() {
        MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseUpdate(MyTunesRss.CONFIG.isIgnoreTimestamps());
        try {
            if (!MyTunesRss.EXECUTOR_SERVICE.getDatabaseUpdateResult()) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.updateNotRun"));
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error during database update", e);
            }
        }
    }

    public static void setCodewaveLogLevel(Level level) {
        if (level == Level.OFF) {
            LOGGER.error("Setting codewave log to level \"" + level + "\".");
        }
        LoggerRepository repository = org.apache.log4j.Logger.getRootLogger().getLoggerRepository();
        for (Enumeration loggerEnum = repository.getCurrentLoggers(); loggerEnum.hasMoreElements();) {
            org.apache.log4j.Logger logger = (org.apache.log4j.Logger) loggerEnum.nextElement();
            if (logger.getName().startsWith("de.codewave.")) {
                logger.setLevel(level);
            }
        }
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("de.codewave");
        logger.setLevel(level);
        LOGGER.error("Setting codewave log to level \"" + level + "\".");
    }

    public static String normalize(String text) {
        return StringUtils.isBlank(text) ? text : Normalizer.compose(text, false);
    }

    public static String getBaseType(String contentType) {
        try {
            ContentType type = new ContentType(StringUtils.trimToEmpty(contentType));
            return type.getBaseType();
        } catch (ParseException e) {
            LOGGER.warn("Could not get base type from content type \"" + contentType + "\".", e);
        }
        return "application/octet-stream";
    }

    public static String getBuiltinAddonsPath() {
        return System.getProperty("de.codewave.mytunesrss.addons.builtin", ".");
    }

    public static File getNativeLibPath() {
        return new File(System.getProperty("de.codewave.mytunesrss.native", "./native"));
    }

    public static String getSystemInfo() {
        StringBuilder systemInfo = new StringBuilder();
        systemInfo.append(MyTunesRssUtils.getBundleString(Locale.getDefault(), "sysinfo.quicktime." + Boolean.toString(MyTunesRss.QUICKTIME_PLAYER != null))).append(System.getProperty("line.separator"));
        systemInfo.append(MyTunesRssUtils.getBundleString(Locale.getDefault(), "sysinfo.httplivestreaming." + Boolean.toString(MyTunesRss.HTTP_LIVE_STREAMING_AVAILABLE))).append(System.getProperty("line.separator"));
        return systemInfo.toString();
    }

    public static String getCacheDataPath() throws IOException {
        if (MyTunesRss.COMMAND_LINE_ARGS.containsKey(MyTunesRss.CMD_CACHE_PATH)) {
            return MyTunesRss.COMMAND_LINE_ARGS.get(MyTunesRss.CMD_CACHE_PATH)[0];
        } else {
            return PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        }
    }

    public static String getPreferencesDataPath() throws IOException {
        if (MyTunesRss.COMMAND_LINE_ARGS.containsKey(MyTunesRss.CMD_PREFS_PATH)) {
            return MyTunesRss.COMMAND_LINE_ARGS.get(MyTunesRss.CMD_PREFS_PATH)[0];
        } else {
            return PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
        }
    }

    public static boolean isOtherInstanceRunning(long timeoutMillis) {
        RandomAccessFile lockFile;
        try {
            File file = new File(MyTunesRssUtils.getCacheDataPath() + "/MyTunesRSS.lck");
            lockFile = new RandomAccessFile(file, "rw");
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not check for other running instance.", e);
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
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not check for other running instance.", e);
                }
            } catch (InterruptedException e) {
                // intentionally left blank
            }
        } while (System.currentTimeMillis() < endTime);
        return true;
    }

    /**
     * Check if the specified index is a valid letter pager index. A valid index is
     * in the range from 0 to 8.
     *
     * @param index An index.
     * @return TRUE if the index is a valid letter pager index or FALSE otherwise.
     */
    public static Boolean isLetterPagerIndex(int index) {
        return index >= 0 && index <= 8;
    }

    public static boolean loginLDAP(String userName, String password) {
        LOGGER.debug("Checking authorization with LDAP server.");
        LdapConfig ldapConfig = MyTunesRss.CONFIG.getLdapConfig();
        if (ldapConfig.isValid()) {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://" + ldapConfig.getHost() + ":" + ldapConfig.getPort());
            env.put(Context.SECURITY_AUTHENTICATION, ldapConfig.getAuthMethod().name());
            env.put(Context.SECURITY_PRINCIPAL, MessageFormat.format(ldapConfig.getAuthPrincipal(), userName));
            env.put(Context.SECURITY_CREDENTIALS, password);
            try {
                DirContext ctx = new InitialDirContext(env);
                LOGGER.debug("Checking authorization with LDAP server: authorized!");
                User user = MyTunesRss.CONFIG.getUser(userName);
                if (user == null) {
                    LOGGER.debug("Corresponding user for LDAP \"" + userName + "\" not found.");
                    User template = MyTunesRss.CONFIG.getUser(ldapConfig.getTemplateUser());
                    if (template != null) {
                        LOGGER.debug("Using LDAP template user \"" + template.getName() + "\".");
                        user = (User) template.clone();
                        user.setName(userName);
                        user.setPasswordHash(MyTunesRss.SHA1_DIGEST.digest(UUID.randomUUID().toString().getBytes("UTF-8")));
                        user.setChangePassword(false);
                        LOGGER.debug("Storing new user with name \"" + user.getName() + "\".");
                        MyTunesRss.CONFIG.addUser(user);
                    }
                }
                if (user == null) {
                    LOGGER.error("Could not create new user \"" + userName + "\" from template user \"" + ldapConfig.getTemplateUser() + "\".");
                    return false;
                }
                if (ldapConfig.isFetchEmail()) {
                    LOGGER.debug("Fetching email for user \"" + userName + "\" from LDAP.");
                    SearchControls searchControls = new SearchControls(SearchControls.SUBTREE_SCOPE, 1, ldapConfig.getSearchTimeout(), new String[]{ldapConfig.getMailAttributeName()}, false, false);
                    NamingEnumeration<SearchResult> namingEnum = ctx.search(StringUtils.defaultString(ldapConfig.getSearchRoot()), MessageFormat.format(ldapConfig.getSearchExpression(), userName), searchControls);
                    if (namingEnum.hasMore()) {
                        String email = namingEnum.next().getAttributes().get(ldapConfig.getMailAttributeName()).get().toString();
                        LOGGER.debug("Setting email \"" + email + "\" for user \"" + user.getName() + "\".");
                        user.setEmail(email);
                    }
                }
                return !user.isGroup() && user.isActive();
            } catch (AuthenticationException e) {
                LOGGER.info("LDAP login failed for \"" + userName + "\".");
            } catch (Exception e) {
                LOGGER.error("Could not validate username/password with LDAP server.", e);
            }
        }
        return false;
    }

    public static RegistrationFeedback getRegistrationFeedback(Locale locale) {
        if (MyTunesRss.REGISTRATION.isExpiredPreReleaseVersion()) {
            return new RegistrationFeedback(MyTunesRssUtils.getBundleString(locale, "error.preReleaseVersionExpired"), false);
        } else if (MyTunesRss.REGISTRATION.isExpired()) {
            return new RegistrationFeedback(MyTunesRssUtils.getBundleString(locale, "error.registrationExpired"), false);
        } else if (MyTunesRss.REGISTRATION.isExpirationDate() && !MyTunesRss.REGISTRATION.isReleaseVersion()) {
            return new RegistrationFeedback(MyTunesRssUtils.getBundleString(locale, "info.preReleaseExpiration",
                    MyTunesRss.REGISTRATION.getExpiration(MyTunesRssUtils.getBundleString(
                            locale, "common.dateFormat"))), true);
        } else if (MyTunesRss.REGISTRATION.isExpirationDate() && !MyTunesRss.REGISTRATION.isExpired()) {
            return new RegistrationFeedback(MyTunesRssUtils.getBundleString(locale, "info.expirationInfo",
                    MyTunesRss.REGISTRATION.getExpiration(MyTunesRssUtils.getBundleString(
                            locale, "common.dateFormat"))), true);
        }
        return null;
    }

    public static Playlist[] getPlaylistPath(Playlist playlist, List<Playlist> playlists) {
        List<Playlist> path = new ArrayList<Playlist>();
        for (; playlist != null; playlist = findPlaylistWithId(playlists, playlist.getContainerId())) {
            if (playlist != null) {
                path.add(0, playlist);
            }
        }
        return path.toArray(new Playlist[path.size()]);
    }

    private static Playlist findPlaylistWithId(List<Playlist> playlists, String containerId) {
        for (Playlist playlist : playlists) {
            if (StringUtils.equals(playlist.getId(), containerId)) {
                return playlist;
            }
        }
        return null;
    }

    public static File createTempFile(String suffix) throws IOException {
        return createTempFile(suffix, 300000); // default timeout is 5 minutes
    }

    private static AtomicLong TEMP_FILE_COUNTER = new AtomicLong();

    public static File createTempFile(String suffix, long timeout) throws IOException {
        File tmpDir = new File(MyTunesRssUtils.getCacheDataPath(), MyTunesRss.CACHEDIR_TEMP);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        File tmpFile = File.createTempFile("mytunesrss_", suffix, tmpDir);
        MyTunesRss.TEMP_CACHE.add("tmp_" + TEMP_FILE_COUNTER.incrementAndGet(), tmpFile, timeout);
        return tmpFile;
    }
}