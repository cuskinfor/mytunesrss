package de.codewave.mytunesrss;

import com.ibm.icu.text.Normalizer;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.config.LdapConfig;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.datastore.DatabaseBackup;
import de.codewave.mytunesrss.datastore.OrphanedImageRemover;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.mytunesrss.task.DeleteDatabaseFilesCallable;
import de.codewave.mytunesrss.vlc.VlcPlayerException;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.io.LogStreamCopyThread;
import de.codewave.utils.io.ZipUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggerRepository;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.h2.mvstore.FileStore;
import org.h2.mvstore.MVStore;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * de.codewave.mytunesrss.MyTunesRssUtils
 */
@SuppressWarnings({"OverlyComplexClass", "OverlyCoupledClass"})
public class MyTunesRssUtils {

    public static Map<String, String> IMAGE_TO_MIME = new HashMap<>();
    public static Map<String, String> MIME_TO_SUFFIX = new HashMap<>();

    static {
        IMAGE_TO_MIME.put("jpg", "image/jpeg");
        IMAGE_TO_MIME.put("gif", "image/gif");
        IMAGE_TO_MIME.put("png", "image/png");
    }

    static {
        MIME_TO_SUFFIX.put("image/gif", "gif");
        MIME_TO_SUFFIX.put("image/jpg", "jpg");
        MIME_TO_SUFFIX.put("image/jpeg", "jpg");
        MIME_TO_SUFFIX.put("image/png", "png");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUtils.class);
    private static RandomAccessFile LOCK_FILE;
    private static final int IMAGE_PATH_SPLIT_SIZE = 6;

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
            //noinspection UseOfSystemOutOrSystemErr
            System.err.println(message);
        }
    }

    public static boolean isHeadless() {
        return (MyTunesRss.CONFIG != null && MyTunesRss.CONFIG.isHeadless()) || MyTunesRss.COMMAND_LINE_ARGS.containsKey(MyTunesRss.CMD_HEADLESS) || GraphicsEnvironment.isHeadless();
    }

    public static void showErrorMessage(String message) {
        LOGGER.error(message);
        //noinspection UseOfSystemOutOrSystemErr
        System.err.println(message);
    }

    public static String getBundleString(Locale locale, String key, Object... parameters) {
        if (key == null) {
            return "";
        }
        if (locale == null) {
            locale = Locale.ENGLISH; // default in case of NULL locale
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
        ModalInfoDialog info = new ModalInfoDialog(MyTunesRssUtils.getBundleString(Locale.getDefault(), "taskinfo.shuttingDown"));
        info.show(2000L);
        MyTunesRss.SHUTDOWN_IN_PROGRESS.set(true);
        LOGGER.debug("Shutting down gracefully.");
        MyTunesRss.CONFIG.save();
        try {
            if (MyTunesRss.VLC_PLAYER != null) {
                MyTunesRss.VLC_PLAYER.destroy();
            }
        } catch (VlcPlayerException e) {
            LOGGER.error("Could not destroy VLC player.", e);
        }
        LOGGER.debug("Destroying streaming cache.");
        MyTunesRss.TRANSCODER_CACHE.destroy();
        if (MyTunesRss.FORM != null) {
            MyTunesRss.FORM.hide();
        }
        //noinspection finally
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
            MyTunesRss.LUCENE_TRACK_SERVICE.shutdown();
            if (MyTunesRss.CONFIG.isDefaultDatabase() && MyTunesRss.CONFIG.isDeleteDatabaseOnExit()) {
                try {
                    new DeleteDatabaseFilesCallable().call();
                } catch (IOException e) {
                    LOGGER.error("Could not delete default database files.", e);
                }
            }
            MyTunesRssUtils.removeMvStoreData();
        } catch (IOException e) {
            LOGGER.error("Exception during shutdown.", e);
        } catch (InterruptedException e) {
            LOGGER.error("Exception during shutdown.", e);
        } finally {
            LOGGER.info("Very last log message before shutdown.");
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
        return createStatement(connection, name, Collections.<String, Boolean>emptyMap());
    }

    public static SmartStatement createStatement(Connection connection, String name, final Map<String, Boolean> conditionals) throws SQLException {
        //noinspection unchecked
        return MyTunesRss.STORE.getSmartStatementFactory().createStatement(connection, name, (Map<String, Boolean>) Proxy.newProxyInstance(MyTunesRss.class.getClassLoader(), new Class[]{Map.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("get".equals(method.getName()) && args.length == 1 && args[0] instanceof String) {
                    //noinspection SuspiciousMethodCalls
                    return conditionals.containsKey(args[0]) ? conditionals.get(args[0]) : Boolean.FALSE;
                } else {
                    return method.invoke(conditionals, args);
                }
            }
        }));
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

    public static String getBuiltinAddonsPath() {
        return System.getProperty("de.codewave.mytunesrss.addons.builtin", ".");
    }

    public static boolean lockInstance(long timeoutMillis) {
        try {
            File file = new File(MyTunesRss.CACHE_DATA_PATH + "/MyTunesRSS.lck");
            file.deleteOnExit();
            LOCK_FILE = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            LOGGER.debug("File not found, not locking instance.", e);
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
                LOGGER.debug("Ignoring exception.", e);
            } catch (InterruptedException e) {
                LOGGER.debug("Ignoring exception.", e);
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
            // We have to use Hashtable here since the naming API requires it
            @SuppressWarnings("UseOfObsoleteCollectionType") Hashtable<String, String> env = new Hashtable<>();
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
                LOGGER.info("LDAP login failed for \"" + userName + "\".", e);
            } catch (NamingException e) {
                LOGGER.error("Could not validate username/password with LDAP server.", e);
            } catch (UnsupportedEncodingException e) {
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
        if (playlists != null) {
            for (Playlist playlist : playlists) {
                if (playlist.getContainerId() == null) {
                    count++;
                }
            }
        }
        return count;
    }

    public static File createTempFile(String suffix) throws IOException {
        return File.createTempFile("mytunesrss_", suffix, MyTunesRss.TEMP_CACHE.getBaseDir());
    }

    /**
     * Get a sub list.
     *
     * @param fullList The full list.
     * @param first The first item to return.
     * @param count Number of items to return. If count is less than 1, the rest of the list is returned.
     * @param <T> List element type.
     *
     * @return Sublist starting with the "first" element and with "count" elements.
     */
    public static <T> List<T> getSubList(List<T> fullList, int first, int count) {
        if (count > 0) {
            return fullList.subList(first, Math.min(first + count, fullList.size()));
        } else {
            return fullList.subList(first, fullList.size());
        }
    }

    public static void backupDatabase() throws IOException, SQLException {
        if (!MyTunesRss.CONFIG.isDefaultDatabase()) {
            throw new IllegalStateException("Cannot backup non-default database.");
        }
        if (!MyTunesRss.STORE.isInitialized()) {
            throw new IllegalStateException("Database must already be initialized for starting a backup.");
        }
        final File backupFile = DatabaseBackup.createBackupFile();
        LOGGER.info("Creating H2 database backup \"" + backupFile.getAbsolutePath() + "\".");
        MyTunesRss.STORE.executeStatement(new DataStoreStatement() {
            public void execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement("BACKUP TO ?");
                try {
                    preparedStatement.setString(1, backupFile.getAbsolutePath());
                    preparedStatement.execute();
                } finally {
                    preparedStatement.close();
                }
            }
        });
    }

    public static void restoreDatabaseBackup(DatabaseBackup backup) throws IOException {
        if (!MyTunesRss.CONFIG.isDefaultDatabase()) {
            throw new IllegalStateException("Cannot restore non-default database.");
        }
        if (MyTunesRss.STORE.isInitialized()) {
            throw new IllegalStateException("Database must not be initialized for restoring a backup.");
        }
        LOGGER.info("Restoring database backup from file \"" + backup.getFile().getAbsolutePath() + "\".");
        File databaseDir = new File(MyTunesRss.CACHE_DATA_PATH + "/" + "h2");
        FileUtils.deleteDirectory(databaseDir);
        databaseDir.mkdir();
        ZipUtils.unzip(backup.getFile(), databaseDir);
    }

    public static List<DatabaseBackup> findDatabaseBackups() throws IOException {
        List<DatabaseBackup> backups = new ArrayList<>();
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

    public static int getMaxImageSize(de.codewave.mytunesrss.meta.Image source) throws IOException {
        if (isExecutableGraphicsMagick() && source.getImageFile() != null) {
            return getMaxImageSizeExternalProcess(source.getImageFile());
        } else {
            return getMaxImageSizeJava(source);
        }
    }

    public static int getMaxImageSize(File source) throws IOException {
        if (isExecutableGraphicsMagick()) {
            return getMaxImageSizeExternalProcess(source);
        } else {
            String mimeType = IMAGE_TO_MIME.get(FilenameUtils.getExtension(source.getName()).toLowerCase());
            de.codewave.mytunesrss.meta.Image image = new de.codewave.mytunesrss.meta.Image(mimeType, FileUtils.readFileToByteArray(source));
            return getMaxImageSizeJava(image);
        }
    }

    private static int getMaxImageSizeJava(de.codewave.mytunesrss.meta.Image source) throws IOException {
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

    private static int getMaxImageSizeExternalProcess(File source) throws IOException {
        List<String> resizeCommand = Arrays.asList(MyTunesRss.CONFIG.getGmExecutable().getAbsolutePath(), "identify", "-format",  "%w %h", source.getAbsolutePath());
        String msg = "Executing command \"" + StringUtils.join(resizeCommand, " ") + "\".";
        LOGGER.debug(msg);
        Process process = new ProcessBuilder(resizeCommand).start();
        MyTunesRss.SPAWNED_PROCESSES.add(process);
        InputStream is = process.getInputStream();
        try {
            LogStreamCopyThread stderrCopyThread = new LogStreamCopyThread(process.getErrorStream(), false, LoggerFactory.getLogger("GM"), LogStreamCopyThread.LogLevel.Error, msg, null);
            stderrCopyThread.setDaemon(true);
            stderrCopyThread.start();
            String[] dimensions = StringUtils.split(StringUtils.trim(IOUtils.toString(is, "UTF-8")));
            if (dimensions.length == 2) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Image dimensions for \"" + source.getAbsolutePath() + "\" are \"" + dimensions[0] + "x" + dimensions[1] + "\".");
                }
                return Math.max(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]));
            } else {
                throw new IOException("Could not get dimension from images using external process.");
            }
        } finally {
            waitForProcess(process, 100);
            MyTunesRss.SPAWNED_PROCESSES.remove(process);
        }
    }

    public static boolean isImageUsable(de.codewave.mytunesrss.meta.Image image) {
        if (!isExecutableGraphicsMagick()) {
            ByteArrayInputStream imageInputStream = new ByteArrayInputStream(image.getData());
            try {
                ImageIO.read(imageInputStream);
            } catch (IOException e) {
                LOGGER.debug("Could not create buffered image.", e);
                return false;
            } finally {
                IOUtils.closeQuietly(imageInputStream);
            }
        }
        return true;
    }

    public static void resizeImageWithMaxSize(de.codewave.mytunesrss.meta.Image source, File target, int maxSize, float jpegQuality, String debugInfo) throws IOException {
        if (isExecutableGraphicsMagick() && source.getImageFile() != null) {
            resizeImageWithMaxSizeExternalProcess(source.getImageFile(), target, maxSize, jpegQuality, debugInfo);
        } else {
            resizeImageWithMaxSizeJava(source, target, maxSize, jpegQuality, debugInfo);
        }
    }

    public static void resizeImageWithMaxSize(File source, File target, int maxSize, float jpegQuality, String debugInfo) throws IOException {
        if (isExecutableGraphicsMagick()) {
            resizeImageWithMaxSizeExternalProcess(source, target, maxSize, jpegQuality, debugInfo);
        } else {
            String mimeType = IMAGE_TO_MIME.get(FilenameUtils.getExtension(source.getName()).toLowerCase());
            de.codewave.mytunesrss.meta.Image image = new de.codewave.mytunesrss.meta.Image(mimeType, FileUtils.readFileToByteArray(source));
            resizeImageWithMaxSizeJava(image, target, maxSize, jpegQuality, debugInfo);
        }
    }

    private static boolean isExecutableGraphicsMagick() {
        return MyTunesRss.CONFIG.isGmEnabled() && MyTunesRss.CONFIG.getGmExecutable() != null && MyTunesRssUtils.canExecute(MyTunesRss.CONFIG.getGmExecutable());
    }

    private static void waitForProcess(final Process process, long maxWaitMillis) {
        try {
            Thread waitForProcessThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        process.waitFor();
                    } catch (InterruptedException e) {
                        LOGGER.debug("Interrupted waiting for process to finish. Destroying process.", e);
                        process.destroy();
                    }
                }
            });
            waitForProcessThread.start();
            waitForProcessThread.join(maxWaitMillis);
        } catch (InterruptedException e) {
            LOGGER.debug("Interrupted waiting for process thread to join. Interrupting thread.", e);
            Thread.currentThread().interrupt();
        } finally {
            process.destroy();
        }
    }

    private static void resizeImageWithMaxSizeExternalProcess(File source, File target, int maxSize, float jpegQuality, String debugInfo) throws IOException {
        long start = System.currentTimeMillis();
        try {
            List<String> resizeCommand = Arrays.asList(MyTunesRss.CONFIG.getGmExecutable().getAbsolutePath(), "convert", source.getAbsolutePath(), "-resize", maxSize + "x" + maxSize, "-quality", Float.toString(jpegQuality), target.getAbsolutePath());
            String msg = "Executing command \"" + StringUtils.join(resizeCommand, " ") + "\".";
            LOGGER.debug(msg);
            final Process process = new ProcessBuilder(resizeCommand).redirectErrorStream(true).start();
            MyTunesRss.SPAWNED_PROCESSES.add(process);
            try {
                LogStreamCopyThread stdoutCopyThread = new LogStreamCopyThread(process.getInputStream(), false, LoggerFactory.getLogger("GM"), LogStreamCopyThread.LogLevel.Info, msg, null);
                stdoutCopyThread.setDaemon(true);
                stdoutCopyThread.start();
                waitForProcess(process, 10000);
            } finally {
                MyTunesRss.SPAWNED_PROCESSES.remove(process);
            }
        } finally {
            LOGGER.debug("Resizing (external process) [" + debugInfo + "] to max " + maxSize + " with jpegQuality " + jpegQuality + " took " + (System.currentTimeMillis() - start) + " ms.");
        }
    }

    private static void resizeImageWithMaxSizeJava(de.codewave.mytunesrss.meta.Image source, File target, int maxSize, float jpegQuality, String debugInfo) throws IOException {
        long start = System.currentTimeMillis();
        ByteArrayInputStream imageInputStream = new ByteArrayInputStream(source.getData());
        try {
            BufferedImage original = ImageIO.read(imageInputStream);
            if (original != null) {
                int width = original.getWidth();
                int height = original.getHeight();
                if (Math.max(width, height) <= maxSize) {
                    FileOutputStream fileOutputStream = new FileOutputStream(target);
                    try {
                        IOUtils.write(source.getData(), fileOutputStream);
                    } finally {
                        fileOutputStream.close();
                    }
                }
                if (width > height) {
                    height = (height * maxSize) / width;
                    width = maxSize;
                } else {
                    width = (width * maxSize) / height;
                    height = maxSize;
                }
                Image scaledImage = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                targetImage.getGraphics().drawImage(scaledImage, 0, 0, null);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                    try {
                        writer.setOutput(new FileImageOutputStream(target));
                        ImageWriteParam param = writer.getDefaultWriteParam();
                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        param.setCompressionQuality(jpegQuality / 100f);
                        param.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
                        writer.write(null, new IIOImage(targetImage, null, null), param);
                    } finally {
                        writer.dispose();
                    }
                } finally {
                    byteArrayOutputStream.close();
                }
            }
        } finally {
            imageInputStream.close();
            LOGGER.debug("Resizing (java) [" + debugInfo  + "] to max " + maxSize + " with jpegQuality " + jpegQuality + " took " + (System.currentTimeMillis() - start) + " ms.");
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
        File composedFile = new File(MiscUtils.compose(filename));
        LOGGER.debug("Trying to find " + MiscUtils.getUtf8UrlEncoded(composedFile.getAbsolutePath()) + ".");
        if (composedFile.exists()) {
            return composedFile;
        }
        File decomposedFile = new File(MiscUtils.decompose(filename));
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
        Set<String> playlistIds = new HashSet<>();
        for (Playlist playlist : session.executeQuery(new FindPlaylistQuery(null, null, null, true)).getResults()) {
            playlistIds.add(playlist.getId());
        }
        Set<String> photoAlbumIds = new HashSet<>(session.executeQuery(new FindPhotoAlbumIdsQuery()));
        for (User user : MyTunesRss.CONFIG.getUsers()) {
            user.retainPlaylists(playlistIds);
            user.retainPhotoAlbums(photoAlbumIds);
        }
    }

    public static List<String> getDefaultVlcCommand(File inputFile) {
        List<String> command = new ArrayList<>();
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
            if (canExecute(file)) {
                LOGGER.info("Found VLC executable \"" + file.getAbsolutePath() + "\".");
                try {
                    return file.getCanonicalPath();
                } catch (IOException e) {
                    LOGGER.warn("Could not get canonical path for VLC file. Using absolute path instead.", e);
                }
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Try to find a GraphicsMagick executable. Depending on the operating system some standard paths are searched.
     *
     * @return The path of a GraphicsMagick executable or NULL if none was found.
     */
    public static String findGraphicsMagickExecutable() {
        File[] files;
        if (SystemUtils.IS_OS_WINDOWS) {
            files = new File[] {
                    /*new File(System.getenv("ProgramFiles") + "/gm.exe"),
                    new File(System.getenv("ProgramFiles") + " (x86)/gm.exe")*/
            };
        } else {
            files = new File[] {
                    new File("/usr/bin/gm"),
                    new File("/opt/local/bin/gm")
            };
        }
        for (File file : files) {
            if (canExecute(file)) {
                LOGGER.info("Found GraphicsMagick executable \"" + file.getAbsolutePath() + "\".");
                try {
                    return file.getCanonicalPath();
                } catch (IOException e) {
                    LOGGER.warn("Could not get canonical path for GM file. Using absolute path instead.", e);
                }
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public static Collection<String> getAvailableListenAddresses() {
        Set<String> result = new HashSet<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces != null && networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                for (Enumeration<InetAddress> addEnum = networkInterface.getInetAddresses(); addEnum.hasMoreElements(); ) {
                    InetAddress inetAddress = addEnum.nextElement();
                    result.add(inetAddress.getHostAddress());
                }
            }
        } catch (SocketException e) {
            LOGGER.warn("Could not get network interfaces.", e);
        }
        return result;
    }

    public static Collection<String> toDatasourceIds(Collection<DatasourceConfig> configs) {
        Set<String> ids = new HashSet<>();
        if (configs != null) {
            for (DatasourceConfig datasourceConfig : configs) {
                ids.add(datasourceConfig.getId());
            }
        }
        return ids;
    }

    public static String toSqlLikeExpression(String text) {
        return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    public static void removeDataForSources(DataStoreSession session, final Set<String> sourceIds) throws SQLException {
        LOGGER.debug("Removing data for " + sourceIds.size() + " datasource(s).");
        OrphanedImageRemover orphanedImageRemover = new OrphanedImageRemover();
        orphanedImageRemover.init();
        try {
            session.executeStatement(new DataStoreStatement() {
                public void execute(Connection connection) throws SQLException {
                    SmartStatement statement = MyTunesRssUtils.createStatement(connection, "removeDataForSourceIds");
                    statement.setItems("sourceIds", sourceIds);
                    statement.execute();
                }
            });
            try {
                MyTunesRss.LUCENE_TRACK_SERVICE.deleteTracksForSourceIds(sourceIds);
            } catch (IOException e) {
                LOGGER.warn("Could not delete tracks from lucene index.", e);
            }
            LOGGER.debug("Recreating help tables.");
            session.executeStatement(new RecreateHelpTablesStatement(true, true, true));
            orphanedImageRemover.remove();
        } finally {
            orphanedImageRemover.destroy();
        }
        LOGGER.debug("Updating statistics.");
        session.executeStatement(new UpdateStatisticsStatement());
    }

    public static Collection<DatasourceConfig> deepClone(Collection<DatasourceConfig> datasourceConfigs) {
        Collection<DatasourceConfig> deepClone = new ArrayList<>();
        for (DatasourceConfig datasourceConfig : datasourceConfigs) {
            deepClone.add(DatasourceConfig.copy(datasourceConfig));
        }
        return deepClone;
    }

    public static boolean canExecute(File file) {
        if (!SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_6)) {
            return file != null && file.exists() && file.isFile(); // the best we can check for if we don't have Java 6 or better
        }
        return file != null && file.exists() && file.isFile() && file.canExecute();
    }

    public static String getLegalFileName(String name) {
        name = name.replace('/', '_');
        name = name.replace('\\', '_');
        name = name.replace('?', '_');
        name = name.replace('*', '_');
        name = name.replace(':', '_');
        name = name.replace('|', '_');
        name = name.replace('\"', '_');
        name = name.replace('<', '_');
        name = name.replace('>', '_');
        name = name.replace('`', '_');
        name = name.replace('\'', '_');
        return name;
    }

    public static String[] substringsBetween(String s, String left, String right) {
        List<String> tokens = new ArrayList<>();
        if (StringUtils.isNotEmpty(s) && StringUtils.isNotEmpty(left) && StringUtils.isNotEmpty(right) && s.length() >= left.length() + right.length()) {
            int k;
            for (int i = s.indexOf(left); i > -1; i = s.indexOf(left, k + right.length())) {
                k = s.indexOf(right, i + 1);
                if (k == -1) {
                    break; // no more end tokens, we are done
                }
                // go right as far as possible while keeping the end boundary
                while (k + 1 + right.length() <= s.length() && right.equals(s.substring(k + 1, k + 1 + right.length()))) {
                    k++;
                }
                tokens.add(s.substring(i + left.length(), k));
                if (k + right.length() >= s.length()) {
                    break; // end of input string reached, we are done
                }
            }
        }
        return tokens.toArray(new String[tokens.size()]);
    }

    public static RequestLogHandler createJettyAccessLogHandler(String prefix, int retainDays, boolean extended, String tz) {
        File accessLogDir = new File(MyTunesRss.CACHE_DATA_PATH + "/accesslogs");
        if (!accessLogDir.exists()) {
            accessLogDir.mkdirs();
        }
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        NCSARequestLog requestLog = new NCSARequestLog(new File(accessLogDir, prefix + "-yyyy_mm_dd.log").getAbsolutePath());
        requestLog.setRetainDays(retainDays);
        requestLog.setAppend(true);
        requestLog.setExtended(extended);
        requestLog.setLogTimeZone(tz);
        requestLogHandler.setRequestLog(requestLog);
        return requestLogHandler;
    }

    public static MVStore.Builder getMvStoreBuilder(String filename) {
        File dir = new File(MyTunesRss.CACHE_DATA_PATH, "mvstore");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, filename);
        if (file.exists()) {
            file.delete();
        }
        return new MVStore.Builder().fileStore(new FileStore()).fileName(file.getAbsolutePath());
    }

    public static void removeMvStoreFile(String filename) {
        File dir = new File(MyTunesRss.CACHE_DATA_PATH, "mvstore");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, filename);
        if (file.exists()) {
            file.delete();
        }
    }

    public static <K, V> Map<K, V> openMvMap(MVStore store, String name) {
        return new InterruptSafeMvMap<>(store.<K, V>openMap(name));
    }

    private static void removeMvStoreData() throws IOException {
        FileUtils.deleteDirectory(new File(MyTunesRss.CACHE_DATA_PATH, "mvstore"));

    }

    public static File getImageDir(String imageHash) {
        File file = new File(MyTunesRss.CACHE_DATA_PATH, "thumbs");
        file.mkdirs();
        while (imageHash.length() > 0) {
            file = new File(file, imageHash.substring(0, Math.min(imageHash.length(), IMAGE_PATH_SPLIT_SIZE)));
            file.mkdirs();
            imageHash = imageHash.substring(Math.min(imageHash.length(), IMAGE_PATH_SPLIT_SIZE));
        }
        return file;
    }

    public static Collection<Integer> getImageSizes(String imageHash) {
        Collection<Integer> sizes = new LinkedHashSet<>();
        for (String filename : getImageDir(imageHash).list()) {
            String basename = FilenameUtils.getBaseName(filename);
            if (basename.startsWith("img")) {
                sizes.add(Integer.parseInt(basename.substring(3)));
            }
        }
        return sizes;
    }

    public static File getMaxSizedImage(String imageHash) {
        File maxSizedFile = null;
        int maxSize = 0;
        for (File file : getImageDir(imageHash).listFiles()) {
            String basename = FilenameUtils.getBaseName(file.getName());
            if (basename.startsWith("img")) {
                int imgSize = Integer.parseInt(basename.substring(3));
                if (imgSize > maxSize) {
                    maxSizedFile = file;
                    maxSize = imgSize;
                }
            }
        }
        return maxSizedFile;
    }

    public static File getImage(String imageHash, int size) {
        if (size > 0) {
            for (File file : getImageDir(imageHash).listFiles()) {
                String basename = FilenameUtils.getBaseName(file.getName());
                if (basename.startsWith("img")) {
                    if (Integer.parseInt(basename.substring(3)) == size) {
                        return file;
                    }
                }
            }
        } else {
            return getMaxSizedImage(imageHash);
        }
        return null;
    }

    public static File getSaveImageFile(String imageHash, int size, String mimeType) {
        File file = getImage(imageHash, size);
        if (file != null) {
            file.delete();
        }
        return new File(getImageDir(imageHash), "img" + size + "." + MIME_TO_SUFFIX.get(mimeType));
    }

    public static void saveImage(String imageHash, int size, String mimeType, byte[] data) throws IOException {
        File file = getSaveImageFile(imageHash, size, mimeType);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
            IOUtils.write(data, fileOutputStream);
        } finally {
            fileOutputStream.close();
        }
    }

    public static String[] toLowerCase(String[] s) {
        if (s != null) {
            String[] result = new String[s.length];
            for (int i = 0; i < s.length; i++) {
                result[i] = StringUtils.lowerCase(s[i]);
            }
            return result;
        } else {
            return null;
        }
    }

    public static void asyncPlayCountAndDateUpdate(final String trackId) {
        MyTunesRss.EXECUTOR_SERVICE.execute(new Runnable() {
            public void run() {
                StopWatch.start("Updating play count and refreshing play count/time related smart playlists.");
                try {
                    MyTunesRss.STORE.executeStatement(new UpdatePlayCountAndDateStatement(new String[]{trackId}));
                    MyTunesRss.STORE.executeStatement(new RefreshSmartPlaylistsStatement(RefreshSmartPlaylistsStatement.UpdateType.ON_PLAY));
                } catch (SQLException | RuntimeException e) {
                    LOGGER.info("Could not update play count and/or refresh smart playlists.", e);
                } finally {
                    StopWatch.stop();
                }
            }
        });
    }

    /**
     * Encrypt the path info. The parts of the path info are expected to be url encoded already.
     * Any %2F and %5C will be replaced by %01 and %02 since tomcat does not like those characters in the path info.
     * So the path info decoder will have to replace %01 and %02 with %2F and %5C.
     *
     * @param pathInfo
     *
     * @return
     */
    public static String encryptPathInfo(String pathInfo) {
        String result = pathInfo;
        try {
            if (MyTunesRss.CONFIG.getPathInfoKey() != null) {
                Cipher cipher = Cipher.getInstance(MyTunesRss.CONFIG.getPathInfoKey().getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE, MyTunesRss.CONFIG.getPathInfoKey());
                result = "%7B" + MyTunesRssBase64Utils.encode(cipher.doFinal(pathInfo.getBytes("UTF-8"))) + "%7D";
            }
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not encrypt path info.", e);
            }
        }
        // replace %2F and %5C with %01 and %02 for the reason specified in the java doc
        return result.replace("%2F", "%01").replace("%2f", "%01").replace("%5C", "%02").replace("%5c", "%02");
    }

    public static String createAuthToken(User user) {
        return encryptPathInfo("auth=" + MiscUtils.getUtf8UrlEncoded(MyTunesRssBase64Utils.encode(user.getName()) + " " +
                        MyTunesRssBase64Utils.encode(user.getPasswordHash())));
    }

    public static TranscoderConfig getTranscoder(String activeTranscoders, Track track) {
        if (MyTunesRss.CONFIG.isValidVlcConfig()) {
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                if (isActiveTranscoder(activeTranscoders, config.getName()) && config.isValidFor(track)) {
                    return config;
                }
            }
        }
        if (isActiveTranscoder(activeTranscoders, TranscoderConfig.MEDIA_SERVER_MP3_128.getName()) && TranscoderConfig.MEDIA_SERVER_MP3_128.isValidFor(track)) {
            return TranscoderConfig.MEDIA_SERVER_MP3_128;
        }
        return null;
    }

    public static boolean isActiveTranscoder(String activeTranscoders, String transcoder) {
        return ArrayUtils.contains(StringUtils.split(activeTranscoders, ','), transcoder);
    }

    public static boolean unknown(String trackAlbumOrArtist) {
        return InsertTrackStatement.UNKNOWN.equals(trackAlbumOrArtist);
    }

    public static String virtualTrackName(Track track) {
        if (unknown(track.getArtist())) {
            return track.getName();
        }
        return track.getArtist() + " - " + track.getName();
    }

    private static final String DEFAULT_NAME = "MyTunesRSS";

    public static String virtualAlbumName(Album album) {
        if (unknown(album.getArtist()) && unknown(album.getName())) {
            return DEFAULT_NAME;
        } else if (unknown(album.getArtist()) || album.getArtistCount() > 1) {
            return album.getName();
        }
        return album.getArtist() + " - " + album.getName();
    }

    public static String virtualArtistName(Artist artist) {
        if (unknown(artist.getName())) {
            return DEFAULT_NAME;
        }
        return artist.getName();
    }
}
