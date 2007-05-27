/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.*;
import org.apache.commons.codec.binary.*;

import java.io.*;
import java.util.*;

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
        HttpClient client = MyTunesRssUtils.createHttpClient();
        try {
            client.executeMethod(postMethod);
        } catch (IOException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Could not update mytunesrss.com", e);
            }
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