/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.server;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.common.*;
import de.codewave.utils.servlet.*;
import org.apache.catalina.*;
import org.apache.catalina.connector.*;
import org.apache.catalina.session.*;
import org.apache.catalina.startup.*;
import org.apache.commons.logging.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.server.WebServer
 */
public class WebServer {
    private static final Log LOG = LogFactory.getLog(WebServer.class);

    private Embedded myEmbeddedTomcat;
    private Context myContext;
    private String myLastErrorMessage;

    public synchronized boolean start(int port, Map<String, Object> contextEntries) {
        if (myEmbeddedTomcat == null) {
            try {
                myEmbeddedTomcat = createServer("mytunesrss", null, port, new File("."), "ROOT", "", contextEntries);
                if (myEmbeddedTomcat != null) {

                    myEmbeddedTomcat.start();
                    byte health = checkServerHealth(port);
                    if (health != CheckHealthResult.OK) {
                        myEmbeddedTomcat.stop();
                        myEmbeddedTomcat = null;
                        if (health == CheckHealthResult.EMPTY_LIBRARY) {
                            myLastErrorMessage = MyTunesRss.BUNDLE.getString("error.serverEmptyLibrary");
                        } else {
                            myLastErrorMessage = MyTunesRss.BUNDLE.getString("error.serverHealth");
                        }
                        myEmbeddedTomcat = null;
                        return false;
                    }
                } else {
                    myLastErrorMessage = MyTunesRss.BUNDLE.getString("error.serverHealth");
                    myEmbeddedTomcat = null;
                    return false;
                }
            } catch (LifecycleException e) {
                if (e.getMessage().contains("BindException")) {
                    myLastErrorMessage = MyTunesRss.BUNDLE.getString("error.serverAddressBind");
                } else {
                    myLastErrorMessage = MyTunesRss.BUNDLE.getString("error.serverStart") + e.getMessage();
                }
                myEmbeddedTomcat = null;
                return false;
            } catch (IOException e) {
                myLastErrorMessage = MyTunesRss.BUNDLE.getString("error.serverStart") + e.getMessage();
                myEmbeddedTomcat = null;
                return false;
            }
        }
        return true;
    }

    private byte checkServerHealth(int port) {
        HttpURLConnection connection = null;
        try {
            URL targetUrl = new URL("http://127.0.0.1:" + port + "/mytunesrss/checkHealth");
            if (LOG.isInfoEnabled()) {
                LOG.info("Trying server health URL \"" + targetUrl.toExternalForm() + "\".");
            }
            connection = (HttpURLConnection)targetUrl.openConnection();
            int responseCode = connection.getResponseCode();
            if (LOG.isInfoEnabled()) {
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
                if (LOG.isInfoEnabled()) {
                    LOG.info("Health servlet response code is " + result + " after " + trial + " trials.");
                }
                return result != -1 ? (byte)result : CheckHealthResult.EOF;
            } else {
                return CheckHealthResult.INVALID_HTTP_RESPONSE;
            }
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
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
        engine.addChild(host);
        myContext = server.createContext(webAppContext, webAppName);
        StandardManager sessionManager = new StandardManager();
        sessionManager.setPathname("");
        myContext.setManager(sessionManager);
        host.addChild(myContext);
        server.addEngine(engine);
        Connector connector = server.createConnector(listenAddress, listenPort, false);
        connector.setURIEncoding("UTF-8");
        server.addConnector(connector);
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
            } catch (LifecycleException e) {
                myLastErrorMessage = MyTunesRss.BUNDLE.getString("error.stopServer") + e.getMessage();
                return false;
            }
        }
        return true;
    }

    public synchronized String getLastErrorMessage() {
        return myLastErrorMessage;
    }

    public synchronized boolean isRunning() {
        return myEmbeddedTomcat != null;
    }

    public List<MyTunesRssSessionInfo> getSessionInfos() {
        if (isRunning()) {
            List<MyTunesRssSessionInfo> sessionInfos =
                    new ArrayList<MyTunesRssSessionInfo>((Collection<MyTunesRssSessionInfo>)SessionManager
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