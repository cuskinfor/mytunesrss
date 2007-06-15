package de.codewave.mytunesrss;

import de.codewave.mytunesrss.task.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.swing.*;
import de.codewave.utils.swing.pleasewait.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.logging.*;

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
        System.exit(0);
    }

    public static void shutdownGracefully() {
        if (MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.stopWebserver();
        }
        if (!MyTunesRss.WEBSERVER.isRunning()) {
            MyTunesRss.CONFIG.saveWindowPosition(MyTunesRss.ROOT_FRAME.getLocation());
            MyTunesRss.CONFIG.save();
            MyTunesRss.SERVER_RUNNING_TIMER.cancel();
            final DatabaseBuilderTask databaseBuilderTask = MyTunesRss.createDatabaseBuilderTask();
            if (databaseBuilderTask.isRunning()) {
                MyTunesRssUtils.executeTask(null, MyTunesRssUtils.getBundleString("pleaseWait.finishingUpdate"), null, false, new MyTunesRssTask() {
                    public void execute() {
                        while (databaseBuilderTask.isRunning()) {
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
}