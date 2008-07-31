package de.codewave.mytunesrss;

import de.codewave.mytunesrss.jmx.MyTunesRssJmxUtils;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.mytunesrss.datastore.statement.RemoveOldTempPlaylistsStatement;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.sql.SmartStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.SwingUtils;
import de.codewave.utils.swing.pleasewait.PleaseWaitTask;
import de.codewave.utils.swing.pleasewait.PleaseWaitUtils;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggerRepository;
import org.quartz.SchedulerException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Enumeration;

import com.ibm.icu.text.Normalizer;

/**
 * de.codewave.mytunesrss.MyTunesRssUtils
 */
public class MyTunesRssUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssUtils.class);

    public static void showErrorMessage(String message) {
        if (MyTunesRss.HEADLESS) {
            if (LOG.isErrorEnabled()) {
                LOG.error(message);
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

    public static void showInfoMessage(String message) {
        if (MyTunesRss.HEADLESS) {
            if (LOG.isInfoEnabled()) {
                LOG.info(message);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Shutting down.");
        }
        if (MyTunesRss.STREAMING_CACHE != null) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cleaning up streamig cache.");
                }
                File destinationFile = new File(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/transcoder/cache.xml");
                FileUtils.writeStringToFile(destinationFile, MyTunesRss.STREAMING_CACHE.getContent());
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not write streaming cache contents, all files will be lost on next start.", e);
                }
                MyTunesRss.STREAMING_CACHE.clearCache();
            }
        }
        if (MyTunesRss.QUARTZ_SCHEDULER != null) {
            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Shutting down quartz scheduler.");
                }
                MyTunesRss.QUARTZ_SCHEDULER.shutdown();
            } catch (SchedulerException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not shutdown quartz scheduler.", e);
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Very last log message before shutdown.");
        }
        System.exit(0);
    }

    public static void shutdownGracefully() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Shutting down gracefully.");
        }
        DatabaseBuilderTask.interruptCurrentTask();
        if (MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.stopWebserver();
        }
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRssJmxUtils.stopJmxServer();
            MyTunesRss.CONFIG.setWindowX(MyTunesRss.ROOT_FRAME.getLocation().x);
            MyTunesRss.CONFIG.setWindowY(MyTunesRss.ROOT_FRAME.getLocation().y);
            MyTunesRss.CONFIG.save();
            MyTunesRss.SERVER_RUNNING_TIMER.cancel();
            if (DatabaseBuilderTask.isRunning()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Database still updating... waiting for it to finish.");
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
                    try {
                        LOG.debug("Removing old temporary playlists.");
                        DataStoreSession session = MyTunesRss.STORE.getTransaction();
                        session.executeStatement(new RemoveOldTempPlaylistsStatement());
                        session.commit();
                    } catch (SQLException e) {
                        LOG.error("Could not remove old temporary playlists.", e);
                    }
                        LOG.debug("Destroying store.");
                    MyTunesRss.STORE.destroy();
                }
            });
            MyTunesRss.ROOT_FRAME.dispose();
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
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Error during database update", e);
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
            LOG.error("Setting codewave log to level \"" + level + "\".");
        }
        LoggerRepository repository = org.apache.log4j.Logger.getRootLogger().getLoggerRepository();
        for (Enumeration loggerEnum = repository.getCurrentLoggers(); loggerEnum.hasMoreElements();) {
            org.apache.log4j.Logger logger = (org.apache.log4j.Logger)loggerEnum.nextElement();
            if (logger.getName().startsWith("de.codewave.")) {
                logger.setLevel(level);
            }
        }
        org.apache.log4j.Logger.getLogger("de.codewave").setLevel(level);
        LOG.error("Setting codewave log to level \"" + level + "\".");
    }

    public static String normalize(String text) {
        return StringUtils.isBlank(text) ? text : Normalizer.compose(text, false);
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
}