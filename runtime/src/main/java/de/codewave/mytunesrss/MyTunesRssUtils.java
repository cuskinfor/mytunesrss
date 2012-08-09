package de.codewave.mytunesrss;

import com.ibm.icu.text.Normalizer;
import de.codewave.camel.mp4.Mp4Atom;
import de.codewave.mytunesrss.config.*;
import de.codewave.mytunesrss.datastore.DatabaseBackup;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.meta.*;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.mytunesrss.task.DeleteDatabaseFilesCallable;
import de.codewave.mytunesrss.vlc.VlcPlayerException;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.io.ZipUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.ResultSetType;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggerRepository;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.*;
import java.awt.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * de.codewave.mytunesrss.MyTunesRssUtils
 */
public class MyTunesRssUtils {

    public static final String SYSTEM_PLAYLIST_ID_AUDIO = "system_audio";
    public static final String SYSTEM_PLAYLIST_ID_MOVIES = "system_movies";
    public static final String SYSTEM_PLAYLIST_ID_TVSHOWS = "system_tvshows";
    public static final String SYSTEM_PLAYLIST_ID_DATASOURCE = "system_ds_";

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUtils.class);
    private static RandomAccessFile LOCK_FILE;

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

    public static void showErrorMessageWithDialog(String message) {
        if (!isHeadless()) {
            JOptionPane.showMessageDialog(null, message);
        } else {
            System.err.println(message);
        }
    }

    public static boolean isHeadless() {
        return (MyTunesRss.CONFIG != null && MyTunesRss.CONFIG.isHeadless()) || MyTunesRss.COMMAND_LINE_ARGS.containsKey(MyTunesRss.CMD_HEADLESS) || GraphicsEnvironment.isHeadless();
    }

    public static void showErrorMessage(String message) {
        LOGGER.error(message);
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
        MyTunesRss.SHUTDOWN_IN_PROGRESS.set(true);
        LOGGER.debug("Shutting down gracefully.");
        try {
            if (MyTunesRss.VLC_PLAYER != null) {
                MyTunesRss.VLC_PLAYER.destroy();
            }
        } catch (VlcPlayerException e) {
            LOGGER.error("Could not destroy VLC player.", e);
        }
        MyTunesRss.CONFIG.save();
        if (MyTunesRss.FORM != null) {
            MyTunesRss.FORM.hide();
        }
        try {
            LOGGER.info("Cancelling database jobs.");
            if (MyTunesRss.WEBSERVER != null && MyTunesRss.WEBSERVER.isRunning()) {
                LOGGER.info("Stopping user interface server.");
                MyTunesRss.stopWebserver();
            }
            if (MyTunesRss.ADMIN_SERVER != null) {
                LOGGER.info("Stopping admin interface server.");
                MyTunesRss.stopAdminServer();
            }
            LOGGER.info("Shutting down executor services.");
            MyTunesRss.ROUTER_CONFIG.deleteUserPortMappings();
            MyTunesRss.ROUTER_CONFIG.deleteAdminPortMapping();
            MyTunesRss.EXECUTOR_SERVICE.shutdown();
            if (MyTunesRss.QUARTZ_SCHEDULER != null) {
                try {
                    LOGGER.info("Shutting down quartz scheduler.");
                    MyTunesRss.QUARTZ_SCHEDULER.shutdown();
                } catch (SchedulerException e) {
                    LOGGER.error("Could not shutdown quartz scheduler.", e);
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
                } finally {
                    session.rollback();                    }
                try {
                    LOGGER.debug("Removing old statistic events.");
                    session.executeStatement(new RemoveOldEventsStatement());
                    session.commit();
                } catch (SQLException e) {
                    LOGGER.error("Could not remove old statistic events.", e);
                } finally {
                    session.rollback();                    }
                LOGGER.debug("Destroying store.");
                MyTunesRss.STORE.destroy();
            }
            if (MyTunesRss.CONFIG.isDefaultDatabase() && MyTunesRss.CONFIG.isDeleteDatabaseOnExit()) {
                try {
                    new DeleteDatabaseFilesCallable().call();
                } catch (IOException e) {
                    LOGGER.error("Could not delete default database files.", e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception during shutdown.", e);

        } finally {

            LOGGER.debug("Very last log message before shutdown.");
            System.exit(0);
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
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    if (!deleteRecursivly(subFile)) {
                        return false;
                    }
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

    public static String compose(String text) {
        return StringUtils.isBlank(text) ? text : Normalizer.compose(text, false);
    }

    public static String decompose(String text) {
        return StringUtils.isBlank(text) ? text : Normalizer.decompose(text, false);
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

    public static boolean lockInstance(long timeoutMillis) {
        try {
            File file = new File(MyTunesRss.CACHE_DATA_PATH + "/MyTunesRSS.lck");
            file.deleteOnExit();
            LOCK_FILE = new RandomAccessFile(file, "rw");
        } catch (IOException e) {
            return false;
        }
        long endTime = System.currentTimeMillis() + timeoutMillis;
        do {
            try {
                if (LOCK_FILE.getChannel().tryLock() != null) {
                    return false;
                }
                Thread.sleep(500);
            } catch (IOException e) {
                // intentionally left blank
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
                        user.setPasswordHash(MyTunesRss.SHA1_DIGEST.get().digest(UUID.randomUUID().toString().getBytes("UTF-8")));
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
        } else if (MyTunesRss.REGISTRATION.isExpiredVersion()) {
            return new RegistrationFeedback(MyTunesRssUtils.getBundleString(locale, "error.registrationExpiredVersion"), false);
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

    public static Playlist findParentPlaylist(Playlist playlist, List<Playlist> playlists) {
        if (playlist.getContainerId() == null) {
            return null;
        }
        return findPlaylistWithId(playlists, playlist.getContainerId());
    }

    public static boolean hasChildPlaylists(Playlist playlist, List<Playlist> playlists) {
        for (Playlist each : playlists) {
            if (playlist.getId().equals(each.getContainerId())) {
                return true;
            }
        }
        return false;
    }

    public static int getRootPlaylistCount(List<Playlist> playlists) {
        int count = 0;
        for (Playlist playlist : playlists) {
            if (playlist.getContainerId() == null) {
                count++;
            }
        }
        return count;
    }

    public static File createTempFile(String suffix) throws IOException {
        return createTempFile(suffix, 300000); // default timeout is 5 minutes
    }

    private static AtomicLong TEMP_FILE_COUNTER = new AtomicLong();

    public static File createTempFile(String suffix, long timeout) throws IOException {
        File tmpDir = new File(MyTunesRss.CACHE_DATA_PATH, MyTunesRss.CACHEDIR_TEMP);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        File tmpFile = File.createTempFile("mytunesrss_", suffix, tmpDir);
        MyTunesRss.TEMP_CACHE.add("tmp_" + TEMP_FILE_COUNTER.incrementAndGet(), tmpFile, timeout);
        return tmpFile;
    }

    /**
     * Get a sub list.
     *
     * @param fullList
     * @param first
     * @param count If count is less than 1, the rest of the list is returned.
     * @param <T>
     * @return
     */
    public static <T> List<T> getSubList(List<T> fullList, int first, int count) {
        if (count > 0) {
            return fullList.subList(first, Math.min(first + count, fullList.size()));
        } else {
            return fullList.subList(first, fullList.size());
        }
    }

    public static void backupDatabase() throws IOException, SQLException {
        LOGGER.info("Creating database backup.");
        if (!MyTunesRss.CONFIG.isDefaultDatabase()) {
            throw new IllegalStateException("Cannot backup non-default database.");
        }
        if (!MyTunesRss.STORE.isInitialized()) {
            throw new IllegalStateException("Database must already be initialized for starting a backup.");
        }
        LOGGER.debug("Destroying store before backup.");
        MyTunesRss.STORE.destroy();
        try {
            File databaseDir = new File(MyTunesRss.CACHE_DATA_PATH + "/" + "h2");
            File backupFile = DatabaseBackup.createBackupFile();
            LOGGER.info("Creating H2 database backup \"" + backupFile.getAbsolutePath() + "\".");
            ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(backupFile);
            try {
                ZipUtils.addFilesToZipRecursively("", databaseDir, new FileFilter() {
                    public boolean accept(File file) {
                        if (file.getName().toLowerCase(Locale.ENGLISH).contains(".lock.db")) {
                            return false;
                        }
                        return true;
                    }
                }, zipOutputStream);
            } finally {
                zipOutputStream.close();
            }
        } finally {
            LOGGER.debug("Restarting store after backup.");
            MyTunesRss.STORE.init();
        }
    }

    public static void restoreDatabaseBackup(DatabaseBackup backup) throws IOException {
        LOGGER.info("Restoring database backup from file \"" + backup.getFile().getAbsolutePath() + "\".");
        if (!MyTunesRss.CONFIG.isDefaultDatabase()) {
            throw new IllegalStateException("Cannot restore non-default database.");
        }
        if (MyTunesRss.STORE.isInitialized()) {
            throw new IllegalStateException("Database must not be initialized for restoring a backup.");
        }
        File databaseDir = new File(MyTunesRss.CACHE_DATA_PATH + "/" + "h2");
        FileUtils.deleteDirectory(databaseDir);
        databaseDir.mkdir();
        ZipUtils.unzip(backup.getFile(), databaseDir);
    }

    public static List<DatabaseBackup> findDatabaseBackups() throws IOException {
        List<DatabaseBackup> backups = new ArrayList<DatabaseBackup>();
        File[] files = new File(MyTunesRss.CACHE_DATA_PATH).listFiles();
        if (files != null) {
            for (File file : files) {
                if (DatabaseBackup.isBackupFile(file)) {
                    LOGGER.debug("Found backup file \"" + file + "\".");
                    backups.add(new DatabaseBackup(file));
                }
            }
        }
        Collections.sort(backups);
        return backups;
    }

    public static void removeAllButLatestDatabaseBackups(int numberOfBackupsToKeep) throws IOException {
        List<DatabaseBackup> backups = findDatabaseBackups();
        if (backups.size() > numberOfBackupsToKeep) {
            LOGGER.info("Deleting " + (backups.size() - numberOfBackupsToKeep) + " old database backup files.");
            for (int i = numberOfBackupsToKeep; i < backups.size(); i++) {
                LOGGER.debug("Deleting backup file \"" + backups.get(i).getFile() + "\".");
                backups.get(i).getFile().delete();
            }
        }
    }

    public static Map<String, Mp4Atom> toMap(Collection<Mp4Atom> atoms) {
        Map<String, Mp4Atom> result = new HashMap<String, Mp4Atom>();
        for (Mp4Atom atom : atoms) {
            result.put(atom.getPath(), atom);
            result.putAll(toMap(atom.getChildren()));
        }
        return result;
    }

    public static int getMaxImageSize(de.codewave.mytunesrss.meta.Image source) throws IOException {
        ByteArrayInputStream imageInputStream = new ByteArrayInputStream(source.getData());
        try {
            BufferedImage original = ImageIO.read(imageInputStream);
            int width = original.getWidth();
            int height = original.getHeight();
            return Math.max(width, height);
        } finally {
            imageInputStream.close();
        }
    }

    public static de.codewave.mytunesrss.meta.Image resizeImageWithMaxSize(de.codewave.mytunesrss.meta.Image source, int maxSize) throws IOException {
        ByteArrayInputStream imageInputStream = new ByteArrayInputStream(source.getData());
        try {
            BufferedImage original = ImageIO.read(imageInputStream);
            int width = original.getWidth();
            int height = original.getHeight();
            if (Math.max(width, height) <= maxSize) {
                return source; // original does not exceed max size
            }
            if (width > height) {
                height = (height * maxSize) / width;
                width = maxSize;
            } else {
                width = (width * maxSize) / height;
                height = maxSize;
            }
            Image scaledImage = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            targetImage.getGraphics().drawImage(scaledImage, 0, 0, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                ImageIO.write(targetImage, "png", byteArrayOutputStream);
                return new de.codewave.mytunesrss.meta.Image("image/png", byteArrayOutputStream.toByteArray());
            } finally {
                byteArrayOutputStream.close();
            }
        } finally {
            imageInputStream.close();
        }
    }

    /**
     * Return best file for the specified name. First the file is created using the composed form of the
     * file name. If this file does not exist, the decomposed form is used. If this file does not exist
     * either, the original name is used. This file is returned no matter whether or not it exists.
     *
     * @param filename A filename.
     *
     * @return The best file for the specified name.
     */
    public static File searchFile(String filename) {
        return searchFile(new File(filename));
    }

    public static File searchFile(File file) {
        String filename = file.getAbsolutePath();
        LOGGER.debug("Trying to find " + MiscUtils.getUtf8UrlEncoded(file.getAbsolutePath()) + ".");
        if (file.exists()) {
            return file;
        }
        File composedFile = new File(MyTunesRssUtils.compose(filename));
        LOGGER.debug("Trying to find " + MiscUtils.getUtf8UrlEncoded(composedFile.getAbsolutePath()) + ".");
        if (composedFile.exists()) {
            return composedFile;
        }
        File decomposedFile = new File(MyTunesRssUtils.decompose(filename));
        LOGGER.debug("Trying to find " + MiscUtils.getUtf8UrlEncoded(decomposedFile.getAbsolutePath()) + ".");
        if (decomposedFile.exists()) {
            return decomposedFile;
        }
        if (file.getParentFile() != null) {
            LOGGER.debug("File not found, trying to find parent.");
            File parent = searchFile(file.getParentFile());
            if (parent != null && parent.isDirectory()) {
                LOGGER.debug("Found parent, listing files.");
                File[] files = parent.listFiles();
                if (files != null) {
                    for (File each : files) {
                        LOGGER.debug("Comparing " + MiscUtils.getUtf8UrlEncoded(file.getName()) + " to " + MiscUtils.getUtf8UrlEncoded(each.getName()) +  ".");
                        if (Normalizer.compare(file.getName(), each.getName(), Normalizer.FOLD_CASE_DEFAULT) == 0) {
                            LOGGER.debug("Match.");
                            return each;
                        }
                    }
                }
            }
        }
        LOGGER.debug("File not found.");
        return file;
    }

    public static void updateUserDatabaseReferences(DataStoreSession session) throws SQLException {
        Set<String> playlistIds = new HashSet<String>();
        for (Playlist playlist : session.executeQuery(new FindPlaylistQuery(null, null, null, true)).getResults()) {
            playlistIds.add(playlist.getId());
        }
        Set<String> photoAlbumIds = new HashSet<String>(session.executeQuery(new FindPhotoAlbumIdsQuery()));
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            user.retainPlaylists(playlistIds);
            user.retainPhotoAlbums(photoAlbumIds);
        }
    }

    public static void createMissingSystemPlaylists(DataStoreSession session) throws SQLException {
        // audio
        if (session.executeQuery(new FindPlaylistQuery(null, SYSTEM_PLAYLIST_ID_AUDIO, null, true)).getResultSize() == 0) {
            LOGGER.info("Creating system playlist for audio tracks.");
            Collection<SmartInfo> smartInfos = new ArrayList<SmartInfo>();
            smartInfos.add(new SmartInfo(SmartFieldType.mediatype, MediaType.Audio.name(), false));
            session.executeStatement(new SaveSystemSmartPlaylistStatement(SYSTEM_PLAYLIST_ID_AUDIO, smartInfos));
            session.executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, SYSTEM_PLAYLIST_ID_AUDIO));
            session.commit();
        }
        // movies
        if (session.executeQuery(new FindPlaylistQuery(null, SYSTEM_PLAYLIST_ID_MOVIES, null, true)).getResultSize() == 0) {
            LOGGER.info("Creating system playlist for movies.");
            Collection<SmartInfo> smartInfos = new ArrayList<SmartInfo>();
            smartInfos.add(new SmartInfo(SmartFieldType.mediatype, MediaType.Video.name(), false));
            smartInfos.add(new SmartInfo(SmartFieldType.videotype, VideoType.Movie.name(), false));
            session.executeStatement(new SaveSystemSmartPlaylistStatement(SYSTEM_PLAYLIST_ID_MOVIES, smartInfos));
            session.executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, SYSTEM_PLAYLIST_ID_MOVIES));
            session.commit();
        }
        // tv shows
        if (session.executeQuery(new FindPlaylistQuery(null, SYSTEM_PLAYLIST_ID_TVSHOWS, null, true)).getResultSize() == 0) {
            LOGGER.info("Creating system playlist for tv shows.");
            Collection<SmartInfo> smartInfos = new ArrayList<SmartInfo>();
            smartInfos.add(new SmartInfo(SmartFieldType.mediatype, MediaType.Video.name(), false));
            smartInfos.add(new SmartInfo(SmartFieldType.videotype, VideoType.TvShow.name(), false));
            session.executeStatement(new SaveSystemSmartPlaylistStatement(SYSTEM_PLAYLIST_ID_TVSHOWS, smartInfos));
            session.executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, SYSTEM_PLAYLIST_ID_TVSHOWS));
            session.commit();
        }
        // data sources
        refreshDatasourcePlaylists(session);
    }

    public static void refreshDatasourcePlaylists(DataStoreSession session) throws SQLException {
        // data sources
        for (DatasourceConfig datasourceConfig : MyTunesRss.CONFIG.getDatasources()) {
            if (session.executeQuery(new FindPlaylistQuery(null, SYSTEM_PLAYLIST_ID_DATASOURCE + datasourceConfig.getId(), null, true)).getResultSize() == 0) {
                LOGGER.info("Creating system playlist for data source \"" + datasourceConfig.getId() + "\".");
                Collection<SmartInfo> smartInfos = new ArrayList<SmartInfo>();
                smartInfos.add(new SmartInfo(SmartFieldType.datasource, datasourceConfig.getId(), false));
                session.executeStatement(new SaveSystemSmartPlaylistStatement(SYSTEM_PLAYLIST_ID_DATASOURCE + datasourceConfig.getId(), smartInfos));
                session.executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, SYSTEM_PLAYLIST_ID_DATASOURCE + datasourceConfig.getId()));
                session.commit();
            }
        }
    }

    public static List<String> getDefaultVlcCommand(File inputFile) {
        List<String> command = new ArrayList<String>();
        command.add(MyTunesRss.CONFIG.getVlcExecutable().getAbsolutePath());
        command.add(inputFile.getAbsolutePath());
        command.add("vlc://quit");
        command.add("--intf=dummy");
        if (SystemUtils.IS_OS_WINDOWS) {
            command.add("--dummy-quiet");
        }
        return command;
    }

    /**
     * Try to find a VLC executable. Depending on the operating system some standard paths are searched.
     *
     * @return The path of a VLC executable or NULL if none was found.
     */
    public static String findVlcExecutable() {
        File[] files;
        if (SystemUtils.IS_OS_MAC_OSX) {
            files = new File[] {
                    new File("/Applications/VLC.app/Contents/MacOS/VLC")
            };
        } else if (SystemUtils.IS_OS_WINDOWS) {
            files = new File[] {
                    new File(System.getenv("ProgramFiles") + "/VideoLAN/VLC/vlc.exe"),
                    new File(System.getenv("ProgramFiles") + " (x86)/VideoLAN/VLC/vlc.exe")
            };
        } else {
            files = new File[] {
                    new File("/usr/bin/vlc")
            };
        }
        for (File file : files) {
            if (MyTunesRssConfig.isVlc(file, true)) {
                try {
                    return file.getCanonicalPath();
                } catch (IOException e) {
                    LOGGER.warn("Could not get canonical path for VLC file. Using absolute path instead.");
                }
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static Collection<String> getAvailableListenAddresses() {
        Set<String> result = new HashSet<String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces != null && networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress inetAddress = interfaceAddress.getAddress();
                    result.add(inetAddress.getHostAddress());
                }
            }
        } catch (SocketException e) {
            LOGGER.warn("Could not get network interfaces.", e);
        }
        return result;
    }

    public static Collection<String> toDatasourceIds(Collection<DatasourceConfig> configs) {
        Set<String> ids = new HashSet<String>();
        for (DatasourceConfig datasourceConfig : configs) {
            ids.add(datasourceConfig.getId());
        }
        return ids;
    }

    public static String toSqlLikeExpression(String text) {
        return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}