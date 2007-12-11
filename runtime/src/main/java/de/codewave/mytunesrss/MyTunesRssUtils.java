package de.codewave.mytunesrss;

import de.codewave.mytunesrss.task.*;
import de.codewave.mytunesrss.jmx.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.pleasewait.*;
import de.codewave.utils.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.logging.*;
import org.apache.commons.io.*;

import javax.swing.*;
import java.io.*;
import java.sql.*;
import java.text.*;

/**
 * de.codewave.mytunesrss.MyTunesRssUtils
 */
public class MyTunesRssUtils {
    private static final Log LOG = LogFactory.getLog(MyTunesRssUtils.class);

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
            MyTunesRss.CONFIG.saveWindowPosition(MyTunesRss.ROOT_FRAME.getLocation());
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
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Destroying store.");
                    }
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
                        DatabaseBuilderTask task = MyTunesRss.createDatabaseBuilderTask();
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
}