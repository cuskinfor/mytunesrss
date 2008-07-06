package de.codewave.mytunesrss.anonystat;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.SystemInformation;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * de.codewave.mytunesrss.anonystat.AnonyStatUtils
 */
public class AnonyStatUtils {
    private static final Log LOG = LogFactory.getLog(AnonyStatUtils.class);
    private static final String URL = "http://mytunesrss.com/tools/anonystat.php";
    private static final AtomicInteger failureCount = new AtomicInteger(0);
    private static final int MAXIMUM_FAILURES_IN_A_ROW = 3;
    private static AtomicBoolean disabled = new AtomicBoolean(false);

    public static void sendApplicationStarted() {
        sendAsync("applicationStarted",
                  "v=" + MyTunesRss.VERSION + ",dbtype=" + MyTunesRss.CONFIG.getDatabaseType());
    }

    public static void sendPlayTrack(String transcoderId, String type) {
        sendAsync("playTrack", "tc=" + transcoderId + ",type=" + type);
    }

    public static void sendDatabaseUpdated(long time, SystemInformation systemInformation) {
        sendAsync("databaseUpdated",
                  "type=" + MyTunesRss.CONFIG.getDatabaseType() + ",time=" + (time / 1000L) + ",tracks=" + systemInformation.getTrackCount());
    }

    private static void sendAsync(final String command, final String data) {
        if (MyTunesRss.CONFIG.isSendAnonyStat() && !disabled.get()) {
            new Thread(new Runnable() {
                public void run() {
                    HttpClient client = MyTunesRssUtils.createHttpClient();
                    PostMethod postMethod = new PostMethod(URL);
                    postMethod.addParameter("command", command);
                    postMethod.addParameter("data", data);
                    try {
                        LOG.debug("Sending statistics: \"" + command + "\".");
                        client.executeMethod(postMethod);
                        failureCount.set(0);
                    } catch (IOException e) {
                        if (failureCount.incrementAndGet() == MAXIMUM_FAILURES_IN_A_ROW) {
                            disabled.set(true);
                        }
                    } finally {
                        postMethod.releaseConnection();
                    }
                }
            }, "AnonyStatSender[" + command + "]").start();
        }
    }
}
