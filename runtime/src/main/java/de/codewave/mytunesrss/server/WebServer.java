/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.server;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssConfig;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.RegistrationFeedback;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.quicktime.QuicktimePlayerException;
import de.codewave.utils.servlet.SessionManager;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * de.codewave.mytunesrss.server.WebServer
 */
public class WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private Server myServer;
    private AtomicBoolean myRunning = new AtomicBoolean(false);
    private WebAppContext myContext;

    public synchronized boolean start() {
        RegistrationFeedback feedback = MyTunesRssUtils.getRegistrationFeedback(Locale.getDefault());
        if (!myRunning.get() && (feedback == null || feedback.isValid())) {
            if (MyTunesRss.CONFIG.getPort() < MIN_PORT || MyTunesRss.CONFIG.getPort() > MAX_PORT) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.illegalServerPort"));
            } else {

                try {
                    myServer = new Server(MyTunesRss.CONFIG.getPort());
                    myContext = new WebAppContext("webapps/ROOT", "/");
                    myContext.setSystemClasses((String[]) ArrayUtils.add(myContext.getSystemClasses(), "de.codewave."));
                    myContext.setAttribute(MyTunesRssConfig.class.getName(), MyTunesRss.CONFIG);
                    myContext.setAttribute(MyTunesRssDataStore.class.getName(), MyTunesRss.STORE)  ;
                    myServer.setHandler(myContext);
                    myServer.start();
                    byte health = checkServerHealth(MyTunesRss.CONFIG.getPort(), true);
                    if (health != CheckHealthResult.OK) {
                        stop();
                        myServer = null;
                        if (health == CheckHealthResult.NULL_DATA_STORE) {
                            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.serverNullDataStore"));
                        } else {
                            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.serverStart"));
                        }
                        return false;
                    }
                    MyTunesRss.ROUTER_CONFIG.addUserPortMappings();
                    myRunning.set(true);
                    if (MyTunesRss.QUICKTIME_PLAYER != null) {
                        MyTunesRss.QUICKTIME_PLAYER.init();
                    }
                } catch (Exception e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could start user server.", e);
                    }
                    // TODO clear url in gui
                    return false;
                }
                int localPort = myServer.getConnectors()[0].getLocalPort();
                // TODO set url in gui
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Started user server on port " + localPort + ".");
                }
                return true;
            }
        }
        return false;
    }

    private byte checkServerHealth(int port, boolean logging) {
        HttpURLConnection connection = null;
        try {
            URL targetUrl = new URL("http://127.0.0.1:" + port + StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebappContext()) + "/mytunesrss/checkHealth?ignoreSession=true");
            if (LOGGER.isInfoEnabled() && logging) {
                LOGGER.info("Trying server health URL \"" + targetUrl.toExternalForm() + "\".");
            }
            connection = (HttpURLConnection) targetUrl.openConnection();
            int responseCode = connection.getResponseCode();
            if (LOGGER.isInfoEnabled() && logging) {
                LOGGER.info("HTTP response code is " + responseCode);
            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                int result = -1;
                int trial = 0;
                while (result == -1 && trial < 10) {
                    result = inputStream.read();
                    trial++;
                    if (result == -1) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // intentionally left blank
                        }
                    }
                }
                if (LOGGER.isInfoEnabled() && logging) {
                    LOGGER.info("Health servlet response code is " + result + " after " + trial + " trials.");
                }
                return result != -1 ? (byte) result : CheckHealthResult.EOF;
            } else {
                return CheckHealthResult.INVALID_HTTP_RESPONSE;
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled() && logging) {
                LOGGER.error("Could not get a proper server health status.", e);
            }
            return CheckHealthResult.SERVER_COMMUNICATION_FAILURE;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public synchronized boolean stop() {
        if (myServer != null) {
            try {
                MyTunesRss.ROUTER_CONFIG.deleteAdminPortMapping();
                myServer.stop();
                myServer.join();
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Cannot stop user server.", e);
                }
                return false;
            }
            myRunning.set(false);
            try {
                if (MyTunesRss.QUICKTIME_PLAYER != null) {
                    MyTunesRss.QUICKTIME_PLAYER.stop();
                    MyTunesRss.QUICKTIME_PLAYER.destroy();
                }
            } catch (QuicktimePlayerException e) {
                LOGGER.error("Could not destroy quicktime player.", e);
            }
        }
        return true;
    }

    public boolean isRunning() {
        return myRunning.get();
    }

    public List<MyTunesRssSessionInfo> getSessionInfos() {
        if (isRunning()) {
            List<MyTunesRssSessionInfo> sessionInfos = new ArrayList<MyTunesRssSessionInfo>((Collection<MyTunesRssSessionInfo>) SessionManager
                    .getAllSessionInfo(myContext.getServletContext()));
            Collections.sort(sessionInfos, new Comparator<MyTunesRssSessionInfo>() {
                public int compare(MyTunesRssSessionInfo sessionInfo, MyTunesRssSessionInfo sessionInfo1) {
                    return (int) (sessionInfo.getConnectTime() - sessionInfo1.getConnectTime());
                }
            });
            return sessionInfos;
        }
        return Collections.emptyList();
    }
}