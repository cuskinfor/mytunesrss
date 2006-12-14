package de.codewave.mytunesrss;

import de.codewave.utils.swing.*;
import de.codewave.utils.swing.pleasewait.*;
import org.apache.commons.logging.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HostConfiguration;

import javax.swing.*;
import java.text.MessageFormat;

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
        } else {
            showErrorMessage(MyTunesRss.ROOT_FRAME, message);
        }
    }

    public static void showErrorMessage(JFrame parent, String message) {
        SwingUtils.showMessage(parent,
                               JOptionPane.ERROR_MESSAGE,
                               MyTunesRss.BUNDLE.getString("error.title"),
                               message,
                               MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH);
    }

    public static void showInfoMessage(JFrame parent, String message) {
        SwingUtils.showMessage(parent,
                               JOptionPane.INFORMATION_MESSAGE,
                               MyTunesRss.BUNDLE.getString("info.title"),
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
                title = MyTunesRss.BUNDLE.getString("pleaseWait.defaultTitle");
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
}