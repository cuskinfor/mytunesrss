package de.codewave.mytunesrss.jmx;

import java.util.List;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 13.02.2007
 *
 * @author Michael Descher
 * @version $Id:$
 */
public interface ServerConfigMBean {
    int getPort();

    void setPort(int port);

    boolean isShowOnLocalNetwork();

    void setShowOnLocalNetwork(boolean showOnLocalNetwork);

    String getServerName();

    void setServerName(String name);

    boolean isRunning();

    String startServer();

    String stopServer();

    boolean isAutostart();

    void setAutostart(boolean autostart);

    boolean isCreateTempArchives();

    void setCreateTempArchives(boolean createTempArchives);

    String[] getLocalAddresses();

    String getExternalAddress();

    int getAjpPort();

    void setAjpPort(int port);

    int getSslPort();

    void setSslPort(int port);

    String getSslKeystoreFile();

    void setSslKeystoreFile(String file);

    String getSslKeystorePassphrase();

    void setSslKeystorePassphrase(String passphrase);

    String getSslKeystoreKeyAlias();

    void setSslKeystoreKeyAlias(String alias);

    String getTomcatProxyHost();

    void setTomcatProxyHost(String host);

    int getTomcatProxyPort();

    void setTomcatProxyPort(int port);

    String getTomcatSslProxyHost();

    void setTomcatSslProxyHost(String host);

    int getTomcatSslProxyPort();

    void setTomcatSslProxyPort(int port);

    String getWebappContext();

    void setWebappContext(String context);

    List<String> getAdditionalContexts();

    String addAdditionalContext(String context, String docbase);

    String removeAdditionalContext(String context);

    int getTomcatMaxThreads();

    void setTomcatMaxThreads(int maxThreads);
}



