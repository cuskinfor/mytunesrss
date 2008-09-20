/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.server;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssConfig;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.utils.PrefsUtils;
import de.codewave.utils.servlet.SessionManager;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.Embedded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.IntrospectionUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * de.codewave.mytunesrss.server.WebServer
 */
public class WebServer {
    private static final Logger LOG = LoggerFactory.getLogger(WebServer.class);
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    private Embedded myEmbeddedTomcat;
    private AtomicBoolean myRunning = new AtomicBoolean(false);
    private Context myContext;

    public synchronized boolean start() {
        if (!myRunning.get()) {
            if (MyTunesRss.CONFIG.getPort() < MIN_PORT || MyTunesRss.CONFIG.getPort() > MAX_PORT) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.illegalServerPort"));
            } else if (MyTunesRss.CONFIG.getUsers() == null || MyTunesRss.CONFIG.getUsers().isEmpty()) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.noUsersFound"));
            } else {
                try {
                    final Map<String, Object> contextEntries = new HashMap<String, Object>();
                    contextEntries.put(MyTunesRssConfig.class.getName(), MyTunesRss.CONFIG);
                    contextEntries.put(MyTunesRssDataStore.class.getName(), MyTunesRss.STORE);
                    String catalinaBase = getCatalinaBase();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Using catalina base: \"" + catalinaBase + "\".");
                    }
                    myEmbeddedTomcat = createServer("mytunesrss",
                                                    null,
                                                    MyTunesRss.CONFIG.getPort(),
                                                    new File(catalinaBase),
                                                    "ROOT",
                                                    MyTunesRss.CONFIG.getWebappContext(),
                                                    contextEntries);
                    if (myEmbeddedTomcat != null) {
                        myEmbeddedTomcat.start();
                        byte health = checkServerHealth(MyTunesRss.CONFIG.getPort(), true);
                        if (health != CheckHealthResult.OK) {
                            myEmbeddedTomcat.stop();
                            myEmbeddedTomcat = null;
                            if (health == CheckHealthResult.NULL_DATA_STORE) {
                                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.serverNullDataStore"));
                            } else {
                                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.serverStart"));
                            }
                            myEmbeddedTomcat = null;
                            return false;
                        }
                        MyTunesRss.CONFIG.save();// save on successful server start
                        myRunning.set(true);
                        return true;
                    } else {
                        MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.serverStart"));
                        myEmbeddedTomcat = null;
                        return false;
                    }
                } catch (LifecycleException e) {
                    if (e.getMessage().contains("BindException")) {
                        MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.serverAddressBind"));
                    } else {
                        MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.serverStart") + e.getMessage());
                    }
                    myEmbeddedTomcat = null;
                    return false;
                } catch (IOException e) {
                    MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.serverStart") + e.getMessage());
                    myEmbeddedTomcat = null;
                    return false;
                }
            }
        }
        return false;
    }

    private String getCatalinaBase() {
        String catalinaBase = getCatalinaBase(null);
        File catalinaBaseFile = new File(catalinaBase);
        if (!catalinaBaseFile.exists() || !catalinaBaseFile.isDirectory()) {
            // try UTF-8 in case resulting dir seems to be non-existant, probably fixes MacOSX problems with special characters
            catalinaBase = getCatalinaBase("UTF-8");
        }
        return catalinaBase;
    }

    private String getCatalinaBase(String encoding) {
        String catalinaBase = null;
        try {
            catalinaBase = encoding != null ? URLDecoder.decode(WebServer.class.getResource("WebServer.class").getFile(), encoding) : URLDecoder.decode(
                    WebServer.class.getResource("WebServer.class").getFile());
        } catch (UnsupportedEncodingException e) {
            catalinaBase = URLDecoder.decode(WebServer.class.getResource("WebServer.class").getFile());
        }
        int index = catalinaBase.toLowerCase().indexOf("mytunesrss.jar");
        if (index > -1) {
            // get the directory containing the main jar file and use it as the catalina base directory
            catalinaBase = catalinaBase.substring(0, index);
            catalinaBase = catalinaBase.split("file:")[catalinaBase.split("file:").length - 1];
            catalinaBase = new File(catalinaBase).getAbsolutePath();
        } else {
            // not started from a jar file, i.e. development environment, use the current working directory as catalina base
            catalinaBase = new File(".").getAbsolutePath();
        }
        return catalinaBase;
    }

    private byte checkServerHealth(int port, boolean logging) {
        HttpURLConnection connection = null;
        try {
            URL targetUrl = new URL("http://127.0.0.1:" + port + MyTunesRss.CONFIG.getWebappContext() +
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
            MyTunesRssUtils
                    .deleteRecursivly(workDir);// at least try to delete the working directory before starting the server to dump outdated stuff
        }
        ((StandardHost)host).setWorkDir(PrefsUtils.getCacheDataPath(MyTunesRss.APPLICATION_IDENTIFIER) + "/tomcat-work");
        engine.addChild(host);
        myContext = server.createContext(webAppContext, webAppName);
        StandardManager sessionManager = new StandardManager();
        sessionManager.setPathname("");
        myContext.setManager(sessionManager);
        host.addChild(myContext);
        for (String contextInfo : MyTunesRss.CONFIG.getAdditionalContexts()) {
            Context context = server.createContext(contextInfo.split(":", 2)[0], contextInfo.split(":", 2)[1]);
            StandardManager extraSessionManager = new StandardManager();
            extraSessionManager.setPathname("");
            context.setManager(extraSessionManager);
            host.addChild(context);
        }
        server.addEngine(engine);
        Connector httpConnector = createConnector(server, listenAddress, listenPort, "http");
        if (httpConnector != null) {
            httpConnector.setAttribute("maxThreads", MyTunesRss.CONFIG.getTomcatMaxThreads());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Setting tomcat HTTP connector maximum threads to " + MyTunesRss.CONFIG.getTomcatMaxThreads() + ".");
            }
            if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getTomcatProxyHost())) {
                LOG.debug("Setting proxy host to \"" + MyTunesRss.CONFIG.getTomcatProxyHost() + "\".");
                httpConnector.setProxyName(MyTunesRss.CONFIG.getTomcatProxyHost());
            }
            if (MyTunesRss.CONFIG.getTomcatProxyPort() > 0 && MyTunesRss.CONFIG.getTomcatProxyPort() < 65536) {
                LOG.debug("Setting proxy port to \"" + MyTunesRss.CONFIG.getTomcatProxyPort() + "\".");
                httpConnector.setProxyPort(MyTunesRss.CONFIG.getTomcatProxyPort());
            }
            httpConnector.setURIEncoding("UTF-8");
            server.addConnector(httpConnector);
            if (MyTunesRss.CONFIG.getTomcatAjpPort() > 0 && MyTunesRss.CONFIG.getTomcatAjpPort() < 65536) {
                Connector ajpConnector = null;
                try {
                    ajpConnector = createConnector(server, listenAddress, MyTunesRss.CONFIG.getTomcatAjpPort(), "ajp");
                    if (ajpConnector != null) {
                        server.addConnector(ajpConnector);
                    }
                } catch (Exception e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Illegal AJP port \"" + MyTunesRss.CONFIG.getTomcatAjpPort() + "\" specified. Connector not added.");
                    }
                }
            }
            if (MyTunesRss.CONFIG.getSslPort() > 0 && MyTunesRss.CONFIG.getSslPort() < 65536) {
                Connector sslConnector = null;
                try {
                    LOG.debug("Adding SSL connector.");
                    sslConnector = createConnector(server, listenAddress, MyTunesRss.CONFIG.getSslPort(), "https");
                    if (sslConnector != null) {
                        LOG.debug("Configuring SSL connector.");
                        if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getTomcatSslProxyHost())) {
                            LOG.debug("Setting SSL proxy host to \"" + MyTunesRss.CONFIG.getTomcatSslProxyHost() + "\".");
                            sslConnector.setProxyName(MyTunesRss.CONFIG.getTomcatSslProxyHost());
                        }
                        if (MyTunesRss.CONFIG.getTomcatSslProxyPort() > 0 && MyTunesRss.CONFIG.getTomcatSslProxyPort() < 65536) {
                            LOG.debug("Setting SSL proxy port to \"" + MyTunesRss.CONFIG.getTomcatSslProxyPort() + "\".");
                            sslConnector.setProxyPort(MyTunesRss.CONFIG.getTomcatSslProxyPort());
                        }
                        sslConnector.setURIEncoding("UTF-8");
                        if (StringUtils.isEmpty(MyTunesRss.CONFIG.getSslKeystoreFile()) || !new File(MyTunesRss.CONFIG.getSslKeystoreFile()).isFile()) {
                            // copy default keystore to configured location
                            LOG.warn("Using default keystore because configured one does not exist but SSL is enabled.");
                            File tempFile = File.createTempFile("mytunesrss-", ".keystore");
                            tempFile.deleteOnExit();
                            IOUtils.copy(getClass().getResourceAsStream("/keystore"), new FileOutputStream(tempFile));
                            sslConnector.setAttribute("keystoreFile", tempFile);
                        } else {
                            sslConnector.setAttribute("keystoreFile", MyTunesRss.CONFIG.getSslKeystoreFile());
                            if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getSslKeystorePass())) {
                                sslConnector.setAttribute("keystorePass", MyTunesRss.CONFIG.getSslKeystorePass());
                            }
                            if (StringUtils.isNotEmpty(MyTunesRss.CONFIG.getSslKeystoreKeyAlias())) {
                                sslConnector.setAttribute("keyAlias", MyTunesRss.CONFIG.getSslKeystoreKeyAlias());
                            }
                        }
                        server.addConnector(sslConnector);
                    }
                } catch (Exception e) {
                    LOG.error("Could not add/configure SSL connector.", e);
                }
            }
            for (Map.Entry<String, Object> contextEntry : contextEntries.entrySet()) {
                myContext.getServletContext().setAttribute(contextEntry.getKey(), contextEntry.getValue());
            }
            myContext.addApplicationListener("de.codewave.mytunesrss.remote.RemoteApiSessionManager");
            return server;
        }
        return null;
    }

    private Connector createConnector(Embedded server, InetAddress listenAddress, int listenPort, String protocol) {
        Connector connector = server.createConnector(listenAddress, listenPort, protocol);
        if (connector == null) {
            // there are quite some internet sites which mention that the above method always returns NULL and
            // provide the following workaround. The above method seems to work in general but on some systems
            // it really seems to return NULL for whatever reason, so I provide the workaround as a fallback solution.
            try {
                connector = new Connector();
                connector.setSecure(false);
                connector.setProtocol(protocol);
                if (listenAddress != null) {
                    IntrospectionUtils.setProperty(connector, "address", listenAddress.getHostAddress());
                }
                IntrospectionUtils.setProperty(connector, "port", Integer.toString(listenPort));
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create connector for \"" + protocol + "\", \"" + listenAddress + "\", \"" + listenPort + "\".", e);
                }
            }
        }
        return connector;
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
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.stopServer") + e.getMessage());
                return false;
            }
        }
        MyTunesRss.CONFIG.save();// save on successful server stop
        myRunning.set(false);
        return true;
    }

    public boolean isRunning() {
        return myRunning.get();
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