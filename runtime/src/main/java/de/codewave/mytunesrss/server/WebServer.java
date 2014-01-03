/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.server;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.config.MyTunesRssConfig;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.vlc.VlcPlayerException;
import de.codewave.utils.servlet.SessionManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.ajp.Ajp13SocketConnector;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * de.codewave.mytunesrss.server.WebServer
 */
public class WebServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);

    private Server myServer;
    private AtomicBoolean myRunning = new AtomicBoolean(false);
    private WebAppContext myContext;

    public synchronized void start() throws Exception {
        RegistrationFeedback feedback = MyTunesRssUtils.getRegistrationFeedback(Locale.getDefault());
        if (feedback != null && !feedback.isValid()) {
            throw new RegistrationException(feedback.getMessage());
        }
        if (!myRunning.get()) {
            ensureValidHttpPortInConfig();
            myServer = new Server();
            myServer.setThreadPool(new QueuedThreadPool(Integer.parseInt(MyTunesRss.CONFIG.getTomcatMaxThreads())));
            if (MyTunesRss.CONFIG.getSslPort() > 0 && MyTunesRss.CONFIG.getSslPort() < 65536) {
                SslContextFactory sslContextFactory = new SslContextFactory();
                if (StringUtils.isEmpty(MyTunesRss.CONFIG.getSslKeystoreFile()) || !new File(MyTunesRss.CONFIG.getSslKeystoreFile()).isFile()) {
                    // copy default keystore to configured location
                    LOGGER.warn("Using default keystore because configured one does not exist but SSL is enabled.");
                    File keystore = new File(MyTunesRss.CACHE_DATA_PATH, "mytunesrss.keystore");
                    IOUtils.copy(getClass().getResourceAsStream("/keystore"), new FileOutputStream(keystore));
                    sslContextFactory.setKeyStore(keystore.getAbsolutePath());
                    sslContextFactory.setKeyStorePassword("changeit");
                } else {
                    sslContextFactory.setKeyStore(MyTunesRss.CONFIG.getSslKeystoreFile());
                    if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getSslKeystorePass())) {
                        sslContextFactory.setKeyStorePassword(MyTunesRss.CONFIG.getSslKeystorePass());
                    }
                    if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getSslKeystoreKeyAlias())) {
                        sslContextFactory.setCertAlias(MyTunesRss.CONFIG.getSslKeystoreKeyAlias());
                    }
                }
                SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(sslContextFactory);
                sslConnector.setPort(MyTunesRss.CONFIG.getSslPort());
                sslConnector.setHost(MyTunesRss.CONFIG.getSslHost());
                myServer.addConnector(sslConnector);
            }
            if (MyTunesRss.CONFIG.getTomcatAjpPort() > 0 && MyTunesRss.CONFIG.getTomcatAjpPort() < 65536) {
                Ajp13SocketConnector ajpConnector = null;
                try {
                    ajpConnector = new Ajp13SocketConnector();
                    ajpConnector.setPort(MyTunesRss.CONFIG.getTomcatAjpPort());
                    ajpConnector.setHost(MyTunesRss.CONFIG.getAjpHost());
                    myServer.addConnector(ajpConnector);
                } catch (Exception e) {
                    LOGGER.error("Illegal AJP port \"" + MyTunesRss.CONFIG.getTomcatAjpPort() + "\" specified. Connector not added.");
                }
            }
            myContext = new WebAppContext("webapps/ROOT", StringUtils.defaultIfEmpty(getContext(), "/"));
            File workDir = new File(MyTunesRss.CACHE_DATA_PATH + "/jetty-user-work");
            if (workDir.exists()) {
                MyTunesRssUtils
                        .deleteRecursivly(workDir);// at least try to delete the working directory before starting the server to dump outdated stuff
            }
            myContext.setTempDirectory(workDir);
            myContext.setSystemClasses((String[]) ArrayUtils.add(myContext.getSystemClasses(), "de.codewave."));
            myContext.setAttribute(MyTunesRssConfig.class.getName(), MyTunesRss.CONFIG);
            myContext.setAttribute(MyTunesRssDataStore.class.getName(), MyTunesRss.STORE);
            myServer.setHandler(myContext);
            SelectChannelConnector httpConnector = new SelectChannelConnector();
            httpConnector.setPort(MyTunesRss.CONFIG.getPort());
            httpConnector.setHost(MyTunesRss.CONFIG.getHost());
            myServer.addConnector(httpConnector);
            myServer.start();
            byte health = checkServerHealth(MyTunesRss.CONFIG.getHost(), MyTunesRss.CONFIG.getPort());
            if (health != CheckHealthResult.OK) {
                stop();
                myServer = null;
                if (health == CheckHealthResult.NULL_DATA_STORE) {
                    throw new NoDatabaseException(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.serverNullDataStore"));
                } else {
                    throw new ServerStartException(MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.serverStart"));
                }
            }
            MyTunesRss.ROUTER_CONFIG.addUserPortMappings();
            myRunning.set(true);
            int localPort = myServer.getConnectors()[0].getLocalPort();
            LOGGER.debug("Started user server on port " + localPort + ".");
        }
    }

    private void ensureValidHttpPortInConfig() throws IOException {
        if (MyTunesRss.CONFIG.getPort() < 1 || MyTunesRss.CONFIG.getPort() > 65535) {
            int freePort = 0;
            int ports[] = {8080, 9090, 10000, 20000, 30000, 40000, 50000, 60000};
            ServerSocket serverSocket = null;
            for (int port : ports) {
                try {
                    serverSocket = new ServerSocket(port);
                    freePort = port;
                    break;
                } catch (BindException e) {
                    // ignore exception, try next port
                } finally {
                    if (serverSocket != null) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                }
            }
            if (freePort == 0) {
                serverSocket = new ServerSocket(0);
                try {
                    freePort = serverSocket.getLocalPort();
                } finally {
                    if (serverSocket != null) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                }
            }
            if (freePort != 0) {
                MyTunesRss.CONFIG.setPort(freePort);
                MyTunesRss.CONFIG.save();
            }
        }
    }

    private byte checkServerHealth(String host, int port) {
        HttpURLConnection connection = null;
        try {
            URL targetUrl = new URL("http://" + StringUtils.defaultIfBlank(host, "127.0.0.1") + ":" + port + getContext() + "/mytunesrss/checkHealth?ignoreSession=true");
            LOGGER.info("Trying server health URL \"" + targetUrl.toExternalForm() + "\".");
            connection = (HttpURLConnection) targetUrl.openConnection();
            int responseCode = connection.getResponseCode();
            LOGGER.info("HTTP response code is " + responseCode);
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
                if (result != -1) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    outputStream.write(result);
                    IOUtils.copyLarge(inputStream, outputStream);
                    if (outputStream.size() > 1) {
                        LOGGER.info("Health servlet response is \"" + IOUtils.toString(new ByteArrayInputStream(outputStream.toByteArray()), "UTF-8"));
                    }
                }
                LOGGER.info("Health servlet response code is " + result + " after " + trial + " trials.");
                return result != -1 ? (byte) result : CheckHealthResult.EOF;
            } else {
                return CheckHealthResult.INVALID_HTTP_RESPONSE;
            }
        } catch (IOException e) {
            LOGGER.error("Could not get a proper server health status.", e);
            return CheckHealthResult.SERVER_COMMUNICATION_FAILURE;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String getContext() {
        String trimmedContext = StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebappContext());
        if ("".equals(trimmedContext) || "/".equals(trimmedContext)) {
            return "";
        } else {
            if (trimmedContext.startsWith("/")) {
                return trimmedContext;
            } else {
                return "/" + trimmedContext;
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
                LOGGER.error("Cannot stop user server.", e);
                return false;
            }
            myRunning.set(false);
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
