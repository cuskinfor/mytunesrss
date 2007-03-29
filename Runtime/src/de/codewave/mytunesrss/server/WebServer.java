/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.server;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.utils.*;
import de.codewave.utils.io.*;
import de.codewave.utils.servlet.*;
import org.apache.catalina.*;
import org.apache.catalina.connector.*;
import org.apache.catalina.core.*;
import org.apache.catalina.session.*;
import org.apache.catalina.startup.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.server.WebServer
 */
public class WebServer {
    private static final Log LOG = LogFactory.getLog(WebServer.class);
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private Embedded myEmbeddedTomcat;
    private Context myContext;

    public synchronized boolean start() {
        if (myEmbeddedTomcat == null) {
            if (MyTunesRss.CONFIG.getPort() < MIN_PORT || MyTunesRss.CONFIG.getPort() > MAX_PORT) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.illegalServerPort"));
            } else if (MyTunesRss.CONFIG.getUsers() == null || MyTunesRss.CONFIG.getUsers().isEmpty()) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.noUsersFound"));
            } else {
                try {
                    final Map<String, Object> contextEntries = new HashMap<String, Object>();
                    contextEntries.put(MyTunesRssConfig.class.getName(), MyTunesRss.CONFIG);
                    contextEntries.put(MyTunesRssDataStore.class.getName(), MyTunesRss.STORE);
                    myEmbeddedTomcat = createServer("mytunesrss", null, MyTunesRss.CONFIG.getPort(), new File("."), "ROOT", System.getProperty(
                            "webapp.context",
                            ""), contextEntries);
                    if (myEmbeddedTomcat != null) {
                        myEmbeddedTomcat.start();
                        byte health = checkServerHealth(MyTunesRss.CONFIG.getPort(), true);
                        if (health != CheckHealthResult.OK) {
                            myEmbeddedTomcat.stop();
                            myEmbeddedTomcat = null;
                            if (health == CheckHealthResult.EMPTY_LIBRARY) {
                                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.serverEmptyLibrary"));
                            } else {
                                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.serverStart"));
                            }
                            myEmbeddedTomcat = null;
                            return false;
                        }
                        return true;
                    } else {
                        MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.serverStart"));
                        myEmbeddedTomcat = null;
                        return false;
                    }
                } catch (LifecycleException e) {
                    if (e.getMessage().contains("BindException")) {
                        MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.serverAddressBind"));
                    } else {
                        MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.serverStart") + e.getMessage());
                    }
                    myEmbeddedTomcat = null;
                    return false;
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.serverStart") + e.getMessage());
                    myEmbeddedTomcat = null;
                    return false;
                }
            }
        }
        return false;
    }

    private byte checkServerHealth(int port, boolean logging) {
        HttpURLConnection connection = null;
        try {
            URL targetUrl = new URL("http://127.0.0.1:" + port + System.getProperty("webapp.context", "") +
                    "/mytunesrss/checkHealth?ignoreSession=true");
            if (LOG.isInfoEnabled() && logging) {
                LOG.info("Trying server health URL \"" + targetUrl.toExternalForm() + "\".");
            }
            connection = (HttpURLConnection)targetUrl.openConnection();
            int responseCode = connection.getResponseCode();
            if (LOG.isInfoEnabled() && logging) {
                LOG.info("HTTP response code is " + responseCode);
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
                if (LOG.isInfoEnabled() && logging) {
                    LOG.info("Health servlet response code is " + result + " after " + trial + " trials.");
                }
                return result != -1 ? (byte)result : CheckHealthResult.EOF;
            } else {
                return CheckHealthResult.INVALID_HTTP_RESPONSE;
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled() && logging) {
                LOG.error("Could not get a proper server health status.", e);
            }
            return CheckHealthResult.SERVER_COMMUNICATION_FAILURE;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private Embedded createServer(String name, InetAddress listenAddress, int listenPort, File catalinaBasePath, String webAppName,
            String webAppContext, Map<String, Object> contextEntries) throws IOException {
        Embedded server = new Embedded();
        server.setCatalinaBase(catalinaBasePath.getCanonicalPath());
        Engine engine = server.createEngine();
        engine.setName("engine." + name);
        engine.setDefaultHost("host." + name);
        Host host = server.createHost("host." + name, new File(catalinaBasePath, "webapps").getCanonicalPath());
        File workDir = new File(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/tomcat-work");
        if (workDir.exists()) {
            MyTunesRssUtils.deleteRecursivly(workDir); // at least try to delete the working directory before starting the server to dump outdated stuff
        }
        ((StandardHost)host).setWorkDir(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/tomcat-work");
        engine.addChild(host);
        myContext = server.createContext(webAppContext, webAppName);
        StandardManager sessionManager = new StandardManager();
        sessionManager.setPathname("");
        myContext.setManager(sessionManager);
        host.addChild(myContext);
        server.addEngine(engine);
        Connector httpConnector = server.createConnector(listenAddress, listenPort, "http");
        httpConnector.setURIEncoding("UTF-8");
        server.addConnector(httpConnector);
        if (StringUtils.isNotEmpty(System.getProperty("ajp.port"))) {
            Connector ajpConnector = null;
            try {
                ajpConnector = server.createConnector(listenAddress, Integer.parseInt(System.getProperty("ajp.port")), "ajp");
                server.addConnector(ajpConnector);
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Illegal AJP port \"" + System.getProperty("ajp.port") + "\" specified. Connector not added.");
                }
            }
        }
        for (Map.Entry<String, Object> contextEntry : contextEntries.entrySet()) {
            myContext.getServletContext().setAttribute(contextEntry.getKey(), contextEntry.getValue());
        }
        return server;
    }

    public synchronized boolean stop() {
        if (myEmbeddedTomcat != null) {
            try {
                myEmbeddedTomcat.stop();
                myEmbeddedTomcat = null;
                byte health = CheckHealthResult.OK;
                while (health != CheckHealthResult.INVALID_HTTP_RESPONSE && health != CheckHealthResult.SERVER_COMMUNICATION_FAILURE) {
                    health = checkServerHealth(MyTunesRss.CONFIG.getPort(), false);
                }
            } catch (LifecycleException e) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.BUNDLE.getString("error.stopServer") + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public synchronized boolean isRunning() {
        return myEmbeddedTomcat != null;
    }

    public List<MyTunesRssSessionInfo> getSessionInfos() {
        if (isRunning()) {
            List<MyTunesRssSessionInfo> sessionInfos = new ArrayList<MyTunesRssSessionInfo>((Collection<MyTunesRssSessionInfo>)SessionManager
                    .getAllSessionInfo(myContext.getServletContext()));
            Collections.sort(sessionInfos, new Comparator<MyTunesRssSessionInfo>() {
                public int compare(MyTunesRssSessionInfo sessionInfo, MyTunesRssSessionInfo sessionInfo1) {
                    return (int)(sessionInfo.getConnectTime() - sessionInfo1.getConnectTime());
                }
            });
            return sessionInfos;
        }
        return Collections.emptyList();
    }
}