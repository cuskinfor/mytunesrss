package de.codewave.mytunesrss;

import com.ibm.icu.text.Normalizer;
import de.codewave.mytunesrss.datastore.external.YouTubeLoader;
import de.codewave.mytunesrss.datastore.statement.RemoveOldTempPlaylistsStatement;
import de.codewave.mytunesrss.jmx.MyTunesRssJmxUtils;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.mytunesrss.task.DeleteDatabaseFilesTask;
import de.codewave.mytunesrss.settings.DialogLayout;
import de.codewave.mytunesrss.settings.SettingsForm;
import de.codewave.systray.SystrayUtils;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.SmartStatement;
import de.codewave.utils.swing.SwingUtils;
import de.codewave.utils.swing.pleasewait.PleaseWaitTask;
import de.codewave.utils.swing.pleasewait.PleaseWaitUtils;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
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
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * de.codewave.mytunesrss.MyTunesRssUtils
 */
public class MyTunesRssUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssUtils.class);

    public static void showErrorMessage(String message) {
        if (MyTunesRss.HEADLESS) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message);
            }
            System.err.println(message);
            MyTunesRss.ERROR_QUEUE.setLastError(message);
        } else {
            showErrorMessage(MyTunesRss.ROOT_FRAME, message);
        }
    }

    public static void showErrorMessage(JFrame parent, String message) {
        SwingUtils.showMessage(parent,
                JOptionPane.ERROR_MESSAGE,
                MyTunesRssUtils.getBundleString("error.title"),
                message,
                MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH);
    }

    public static Object showQuestionMessage(String message, Object... options) {
        return showQuestionMessage(MyTunesRss.ROOT_FRAME, message, options);
    }

    public static Object showQuestionMessage(JFrame parent, String message, Object... options) {
        return SwingUtils.showOptionsMessage(parent,
                JOptionPane.QUESTION_MESSAGE,
                MyTunesRssUtils.getBundleString("question.title"),
                message,
                MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH, options);

    }

    public static void showInfoMessage(String message) {
        if (MyTunesRss.HEADLESS) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(message);
            }
            System.out.println(message);
        } else {
            showInfoMessage(MyTunesRss.ROOT_FRAME, message);
        }
    }


    public static void showInfoMessage(JFrame parent, String message) {
        SwingUtils.showMessage(parent,
                JOptionPane.INFORMATION_MESSAGE,
                MyTunesRssUtils.getBundleString("info.title"),
                message,
                MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH);
    }

    public static void executeTask(String title, String text, String cancelButtonText, boolean progressBar, PleaseWaitTask task) {
        if (MyTunesRss.HEADLESS) {
            try {
                task.execute();
            } catch (Exception e) {
                task.handleException(e);
            }
        } else {
            if (title == null) {
                title = MyTunesRssUtils.getBundleString("pleaseWait.defaultTitle");
            }
            PleaseWaitUtils.executeAndWait(MyTunesRss.ROOT_FRAME, MyTunesRss.PLEASE_WAIT_ICON, title, text, cancelButtonText, progressBar, task);
        }
    }

    public static String getBundleString(String key, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return MyTunesRss.BUNDLE.getString(key);
        }
        return MessageFormat.format(MyTunesRss.BUNDLE.getString(key), parameters);
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
        DatabaseBuilderTask.interruptCurrentTask();
        if (MyTunesRss.WEBSERVER != null && MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.stopWebserver();
        }
        if (MyTunesRss.WEBSERVER == null || !MyTunesRss.WEBSERVER.isRunning()) {
            if (!MyTunesRss.HEADLESS) {
                MyTunesRss.CONFIG.setWindowX(MyTunesRss.ROOT_FRAME.getLocation().x);
                MyTunesRss.CONFIG.setWindowY(MyTunesRss.ROOT_FRAME.getLocation().y);
            }
            MyTunesRss.SERVER_RUNNING_TIMER.cancel();
            if (DatabaseBuilderTask.isRunning()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Database still updating... waiting for it to finish.");
                }
                MyTunesRssUtils.executeTask(null, MyTunesRssUtils.getBundleString("pleaseWait.finishingUpdate"), null, false, new MyTunesRssTask() {
                    public void execute() {
                        while (DatabaseBuilderTask.isRunning()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // intentionally left blank
                            }
                        }
                    }
                });
            }
            if (MyTunesRss.STORE != null && MyTunesRss.STORE.isInitialized()) {
                MyTunesRssUtils.executeTask(null, MyTunesRssUtils.getBundleString("pleaseWait.shutdownDatabase"), null, false, new MyTunesRssTask() {
                    public void execute() {
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
                });
            }
            if (!MyTunesRss.HEADLESS) {
                MyTunesRss.ROOT_FRAME.dispose();
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
                new DeleteDatabaseFilesTask().execute();
            } catch (IOException e) {
                LOGGER.error("Could not delete default database files.");
            }
        }
        if (MyTunesRss.SYSTRAY != null) {
            SystrayUtils.remove(MyTunesRss.SYSTRAY.getUUID());
        }
        MyTunesRssJmxUtils.stopJmxServer();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Very last log message before shutdown.");
        }
        System.exit(0);
    }

    public static void onShutdown() {
        MyTunesRss.CONFIG.save();
        if (MyTunesRss.STREAMING_CACHE != null) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Cleaning up streamig cache.");
                }
                File destinationFile = new File(MyTunesRssUtils.getCacheDataPath() + "/transcoder/cache.xml");
                FileUtils.writeStringToFile(destinationFile, MyTunesRss.STREAMING_CACHE.getContent());
            } catch (IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not write streaming cache contents, all files will be lost on next start.", e);
                }
                MyTunesRss.STREAMING_CACHE.clearCache();
            }
        }
        if (MyTunesRss.ARCHIVE_CACHE != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Cleaning up archive cache.");
            }
            MyTunesRss.ARCHIVE_CACHE.clearCache();
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

    public static int getTextFieldInteger(JTextField textField, int defaultValue) {
        try {
            return Integer.parseInt(textField.getText());
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    public static String getTextFieldString(JTextField textField, String defaultValue, boolean trim) {
        if (StringUtils.isBlank(textField.getText())) {
            return defaultValue;
        }
        return trim ? textField.getText().trim() : textField.getText();
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
        return createStatement(connection, name, Collections.<String, Boolean>emptyMap());
    }

    public static SmartStatement createStatement(Connection connection, String name, final Map<String, Boolean> conditionals) throws SQLException {
        return MyTunesRss.STORE.getSmartStatementFactory().createStatement(connection, name, (Map<String, Boolean>)Proxy.newProxyInstance(MyTunesRss.class.getClassLoader(), new Class[] {Map.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("get".equals(method.getName()) && args.length == 1 && args[0] instanceof String) {
                    return conditionals.containsKey((String)args[0]) ? conditionals.get((String)args[0]) : Boolean.FALSE;
                } else {
                    return method.invoke(conditionals, args);
                }
            }
        }));
    }

    public static void executeDatabaseUpdate() {
        if (!DatabaseBuilderTask.isRunning()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        DatabaseBuilderTask task = new DatabaseBuilderTask();
                        task.execute();
                        if (!task.isExecuted()) {
                            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.updateNotRun"));
                        }
                    } catch (Exception e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Error during database update", e);
                        }
                    }
                }
            }).start();
        } else {
            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.updateAlreadyRunning"));
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
        org.apache.log4j.Logger.getLogger("de.codewave").setLevel(level);
        LOGGER.error("Setting codewave log to level \"" + level + "\".");
    }

    public static String normalize(String text) {
        return StringUtils.isBlank(text) ? text : Normalizer.compose(text, false);
    }

    public static Integer getStringInteger(String text, Integer defaultValue) {
        if (StringUtils.isNotEmpty(text)) {
            return Integer.parseInt(text);
        }
        return defaultValue;
    }

    public static String getValueString(Integer number, Integer minimum, Integer maximum, String defaultText) {
        if (number != null) {
            if (minimum == null || minimum <= number) {
                if (maximum == null || maximum >= number) {
                    return number.toString();
                }
            }
        }
        return StringUtils.trimToEmpty(defaultText);
    }

    public static String getValueString(Long number, Long minimum, Long maximum, String defaultText) {
        if (number != null) {
            if (minimum == null || minimum <= number) {
                if (maximum == null || maximum >= number) {
                    return number.toString();
                }
            }
        }
        return StringUtils.trimToEmpty(defaultText);
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

    public static String getContentTypeFromUrl(String url) {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        try {
            if (client.executeMethod(method) == 200) {
                return getBaseType(method.getResponseHeader("Content-Type").getValue());
            }
        } catch (HttpException e) {
            LOGGER.warn("Could not get content type from url \"" + url + "\".", e);
        } catch (IOException e) {
            LOGGER.warn("Could not get content type from url \"" + url + "\".", e);
        } finally {
            method.releaseConnection();
        }
        return "application/octet-stream";
    }

    /**
     * Check if the specified URL is a valid MyTunesRSS remote datasource url.
     *
     * @param url An url string.
     * @return <code>true</code> if the specified url is a valid MyTunesRSS datasource url or <code>false</code> otherwise.
     */
    public static boolean isValidRemoteUrl(String url) {
        if (StringUtils.startsWithIgnoreCase(url, "http://") || StringUtils.startsWithIgnoreCase(url, "https://")) {
            return StringUtils.isNotBlank(getHost(url));
        }
        return false;
    }

    /**
     * Get the host name of an url string.
     *
     * @param url An url string.
     * @return The host name of the url.
     */
    public static String getHost(String url) {
        String host = StringUtils.substringBetween(url, "://", "/");
        if (StringUtils.isBlank(host)) {
            host = StringUtils.substringAfter(url, "://");
        }
        return StringUtils.trimToNull(host);
    }

    public static String getYouTubeUrl(String trackId) {
        String videoId = StringUtils.substringAfter(trackId, "youtube_");
        return "http://youtube.com/get_video?video_id=" + videoId + "&t=" + YouTubeLoader.retrieveAdditionalParam(videoId) + "&fmt=18";
    }

    public static String getBuiltinAddonsPath() {
        return System.getProperty("de.codewave.mytunesrss.addons.builtin", ".");
    }

    public static String getSystemInfo() {
        StringBuilder systemInfo = new StringBuilder();
        systemInfo.append(MyTunesRssUtils.getBundleString("sysinfo.quicktime." + Boolean.toString(MyTunesRss.QUICKTIME_PLAYER != null))).append(System.getProperty("line.separator"));
        return systemInfo.toString();
    }

    public static String getCacheDataPath() throws IOException {
        if (MyTunesRss.COMMAND_LINE_ARGS.containsKey("cacheDataPath")) {
            return MyTunesRss.COMMAND_LINE_ARGS.get("cacheDataPath")[0];
        }
        return PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
    }

    public static String getPreferencesDataPath() throws IOException {
        if (MyTunesRss.COMMAND_LINE_ARGS.containsKey("preferencesDataPath")) {
            return MyTunesRss.COMMAND_LINE_ARGS.get("preferencesDataPath")[0];
        }
        return PrefsUtils.getPreferencesDataPath(MyTunesRss.APPLICATION_IDENTIFIER);
    }

    public static void shutdownRemoteProcess(String baseUrl) {
        try {
            HttpClient httpClient = new HttpClient();
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(1000);
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(1);
            GetMethod getMethod = new GetMethod(baseUrl + "/invoke?objectname=" + URLEncoder.encode("MyTunesRSS:type=config,name=Application", "UTF-8") + "&operation=quit");
            try {
                httpClient.executeMethod(getMethod);
            } catch (IOException e) {
                // expected exception
            } finally {
                getMethod.releaseConnection();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not stop remote application.");
        }
    }

    public static boolean isOtherInstanceRunning(long timeoutMillis) {
        RandomAccessFile lockFile;
        try {
            File file = new File(MyTunesRssUtils.getCacheDataPath() + "/MyTunesRSS.lck");
            file.deleteOnExit();
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

    public static boolean isNumberRange(long check, long min, long max) {
        return check >= min && check <= max;
    }

    public static void showSettingsForm(final SettingsForm form) {
        form.initValues();
        String dialogTitle = MyTunesRssUtils.getBundleString("dialog.settings.commonTitle", form.getDialogTitle());
        final JDialog dialog = new JDialog(MyTunesRss.ROOT_FRAME, dialogTitle, true);
        dialog.getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogLayout layout = MyTunesRss.CONFIG.getDialogLayout(form.getClass());
                if (layout == null) {
                    layout = MyTunesRss.CONFIG.createDialogLayout(form.getClass());
                }
                layout.setX((int) dialog.getLocation().getX());
                layout.setY((int) dialog.getLocation().getY());
                layout.setWidth((int) dialog.getSize().getWidth());
                layout.setHeight((int) dialog.getSize().getHeight());
                String messages = form.updateConfigFromGui();
                if (messages != null) {
                    String cont = MyTunesRssUtils.getBundleString("question.dialogErrors.continue");
                    String canc = MyTunesRssUtils.getBundleString("question.dialogErrors.cancel");
                    String[] options = new String[]{canc, cont};
                    if (canc.equals(MyTunesRssUtils.showQuestionMessage(messages + MyTunesRssUtils.getBundleString("question.dialogErrors"), options))) {
                        dialog.dispose();
                    }
                } else {
                    dialog.dispose();
                }
            }
        });
        dialog.add(form.getRootPanel());
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        DialogLayout layout = MyTunesRss.CONFIG.getDialogLayout(form.getClass());
        dialog.pack();
        final Dimension minimalDimension = dialog.getSize();
        dialog.setMinimumSize(minimalDimension);
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                Dimension d = e.getComponent().getSize();
                boolean changed = false;
                if (d.width < minimalDimension.width) {
                    d.width = minimalDimension.width;
                    changed = true;
                }
                if (d.height < minimalDimension.height) {
                    d.height = minimalDimension.height;
                    changed = true;
                }
                if (changed) {
                    e.getComponent().setSize(d);
                }
            }
        });
        if (layout != null && layout.isValid()) {
            dialog.setLocation(layout.getX(), layout.getY());
            dialog.setSize(layout.getWidth(), layout.getHeight());
            dialog.setVisible(true);
        } else {
            SwingUtils.packAndShowRelativeTo(dialog, MyTunesRss.ROOT_FRAME);
        }
    }

    /**
     * Check if the specified index is a valid letter pager index. A valid index is
     * in the range from 0 to 8.
     *
     * @param index An index.
     *
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
                        LOGGER.debug("Storing new user with name \"" + user.getName() + "\".");
                        MyTunesRss.CONFIG.addUser(user);
                        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.CONFIGURATION_CHANGED));
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
                return true;
            } catch (AuthenticationException e) {
                LOGGER.info("LDAP login failed for \"" + userName + "\".");
            } catch (Exception e) {
                LOGGER.error("Could not validate username/password with LDAP server.", e);
            }
        }
        return false;
    }
}