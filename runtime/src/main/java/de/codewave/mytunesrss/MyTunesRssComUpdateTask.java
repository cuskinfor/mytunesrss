/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.codec.binary.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * de.codewave.mytunesrss.DatabaseWatchdogTask
 */
public class MyTunesRssComUpdateTask extends TimerTask {
    private static final Log LOG = LogFactory.getLog(MyTunesRssComUpdateTask.class);
    private static final String MYTUNESRSSCOM_URL = MyTunesRss.MYTUNESRSSCOM_TOOLS_URL + "/save_ip.php";

    private int myInterval;
    private Timer myTimer;
    private String myUsername;
    private byte[] myPasswordHash;

    public MyTunesRssComUpdateTask(Timer timer, int interval, String username, byte[] passwordHash) {
        myTimer = timer;
        myInterval = interval;
        myUsername = username;
        myPasswordHash = passwordHash;
    }

    public void run() {
        String base64Hash = null;
        try {
            base64Hash = new String(Base64.encodeBase64(myPasswordHash), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            base64Hash = new String(Base64.encodeBase64(myPasswordHash));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating mytunesrss.com: user=\"" + myUsername + "\", password=\"" + base64Hash + "\", port=\"" + MyTunesRss.CONFIG.getPort() +
                    "\".");
        }
        PostMethod postMethod = new PostMethod(System.getProperty("MyTunesRSS.mytunesrsscomUrl", MYTUNESRSSCOM_URL));
        postMethod.addParameter("user", myUsername);
        postMethod.addParameter("pass", base64Hash);
        postMethod.addParameter("port", Integer.toString(MyTunesRss.CONFIG.getPort()));
        postMethod.addParameter("context", System.getProperty("webapp.context", ""));
        HttpClient client = MyTunesRssUtils.createHttpClient();
        try {
            MyTunesRssEvent event = MyTunesRssEvent.MYTUNESRSS_COM_UPDATED;
            int responseCode = client.executeMethod(postMethod);
            if (responseCode != 200) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Could not update mytunesrss.com (Status code " + responseCode + ").");
                }
                event.setMessageKey("mytunesrsscom.invalidLogin");
            } else {
                event.setMessageKey("mytunesrsscom.updateOk");
                event.setMessageParams(new SimpleDateFormat(MyTunesRssUtils.getBundleString("settings.lastMyTunesRssComUpdateDateFormat")).format(new Date()));
            }
            MyTunesRssEventManager.getInstance().fireEvent(event);
        } catch (IOException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Could not update mytunesrss.com", e);
            }
        } finally {
            postMethod.releaseConnection();
        }
        try {
            myTimer.schedule(new MyTunesRssComUpdateTask(myTimer, myInterval, myUsername, myPasswordHash), myInterval);
        } catch (IllegalStateException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not re-schedule task!", e);
            }
        }
    }
}