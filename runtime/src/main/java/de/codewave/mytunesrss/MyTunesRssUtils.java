package de.codewave.mytunesrss;

import com.ibm.icu.text.Normalizer;
import de.codewave.mytunesrss.datastore.external.YouTubeLoader;
import de.codewave.mytunesrss.datastore.statement.RemoveOldTempPlaylistsStatement;
import de.codewave.mytunesrss.jmx.MyTunesRssJmxUtils;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
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
import java.util.Enumeration;

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

    public static void shutdown() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Shutting down.");
        }
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Very last log message before shutdown.");
        }
        System.exit(0);
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
            MyTunesRssJmxUtils.stopJmxServer();
            if (!MyTunesRss.HEADLESS) {
                MyTunesRss.CONFIG.setWindowX(MyTunesRss.ROOT_FRAME.getLocation().x);
                MyTunesRss.CONFIG.setWindowY(MyTunesRss.ROOT_FRAME.getLocation().y);
            }
            MyTunesRss.CONFIG.save();
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
            if (!MyTunesRss.HEADLESS) {
                MyTunesRss.ROOT_FRAME.dispose();
            }
        }
        shutdown();
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
        return MyTunesRss.STORE.getSmartStatementFactory().createStatement(connection, name);
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
            GetMethod getMethod = new GetMethod(baseUrl + "/invoke?objectname=" + URLEncoder.encode("MyTunesRSS:type=config,name=Application", "UTF-8") + "&operation=quit");
            try {
                LOGGER.debug("Response status = " + httpClient.executeMethod(getMethod));
                LOGGER.debug(getMethod.getResponseBodyAsString());
            } catch (IOException e) {
                LOGGER.error("Could not stop remote application.", e);
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
}