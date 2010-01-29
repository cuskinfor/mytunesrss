package de.codewave.mytunesrss.jmx;

import java.io.UnsupportedEncodingException;

public interface MiscConfigMBean {
    boolean isUseProxy();

    void setUseProxy(boolean useProxy);

    String getProxyHost();

    void setProxyHost(String proxyHost);

    int getProxyPort();

    void setProxyPort(int port);

    String getMyTunesRssComUser();

    void setMyTunesRssComUser(String usermame);

    void setMyTunesRssComPassword(String password) throws UnsupportedEncodingException;

    boolean isMyTunesRssComSslSettings();

    void setMyTunesRssComSslSettings(boolean ssl);

    String getWebWelcomeMessage();

    void setWebWelcomeMessage(String message);

    String getWebLoginMessage();

    void setWebLoginMessage(String message);

    String getMailHost();

    void setMailHost(String mailHost);

    String getMailLogin();

    void setMailLogin(String mailLogin);

    void setMailPassword(String mailPassword);

    int getMailPort();

    void setMailPort(int mailPort);

    String getMailSender();

    void setMailSender(String mailSender);

    String getSmtpProtocol();

    void setSmtpProtocol(String smtpProtocol);

    boolean isMinimizeToSystray();

    void setMinimizeToSystray(boolean minimizeToSystray);

    boolean isServerBrowserActive();

    void setServerBrowserActive(boolean serverBrowserActive);
}



