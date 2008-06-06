package de.codewave.mytunesrss.anonystat;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * de.codewave.mytunesrss.anonystat.AnonyStatUtils
 */
public class AnonyStatUtils {
    private static final Log LOG = LogFactory.getLog(AnonyStatUtils.class);
    private static final String URL = "http://mytunesrss.com/tools/anonystat.php";

    public static void sendApplicationStarted() {
        if (MyTunesRss.CONFIG.isSendAnonyStat()) {
            new Thread(new Runnable() {
                public void run() {
                    HttpClient client = MyTunesRssUtils.createHttpClient();
                    PostMethod postMethod = new PostMethod(URL);
                    postMethod.addParameter("command", "applicationStarted");
                    postMethod.addParameter("data",
                                            "v=" + MyTunesRss.VERSION + ",reg=" + MyTunesRss.REGISTRATION.isRegistered() + ",dbtype=" +
                                                    MyTunesRss.CONFIG.getDatabaseType());
                    try {
                        LOG.debug("Sending statistics: \"applicationStarted\".");
                        client.executeMethod(postMethod);
                    } catch (IOException e) {
                        // intentionally left blank
                    } finally {
                        postMethod.releaseConnection();
                    }
                }
            }).start();
        }
    }

    public static void sendPlayTrack(final String transcoderId, final String type) {
        if (MyTunesRss.CONFIG.isSendAnonyStat()) {
            new Thread(new Runnable() {
                public void run() {
                    HttpClient client = MyTunesRssUtils.createHttpClient();
                    PostMethod postMethod = new PostMethod(URL);
                    postMethod.addParameter("command", "playTrack");
                    postMethod.addParameter("data", "tc=" + transcoderId + ",type=" + type);
                    try {
                        LOG.debug("Sending statistics: \"playTrack\".");
                        client.executeMethod(postMethod);
                    } catch (IOException e) {
                        // intentionally left blank
                    } finally {
                        postMethod.releaseConnection();
                    }
                }
            }).start();
        }
    }

    public static void sendDatabaseUpdated(final long time, final SystemInformation systemInformation) {
        if (MyTunesRss.CONFIG.isSendAnonyStat()) {
            new Thread(new Runnable() {
                public void run() {
                    HttpClient client = MyTunesRssUtils.createHttpClient();
                    PostMethod postMethod = new PostMethod(URL);
                    postMethod.addParameter("command", "databaseUpdated");
                    postMethod.addParameter("data",
                                            "type=" + MyTunesRss.CONFIG.getDatabaseType() + ",time=" + (time / 1000L) + ",tracks=" +
                                                    systemInformation.getTrackCount());
                    try {
                        LOG.debug("Sending statistics: \"databaseUpdated\".");
                        client.executeMethod(postMethod);
                    } catch (IOException e) {
                        // intentionally left blank
                    } finally {
                        postMethod.releaseConnection();
                    }
                }
            }).start();
        }
    }
}
