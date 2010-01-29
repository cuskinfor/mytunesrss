package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.SmtpProtocol;
import org.apache.commons.lang.StringUtils;

import javax.management.NotCompliantMBeanException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class MiscConfig extends MyTunesRssMBean implements MiscConfigMBean {
    MiscConfig() throws NotCompliantMBeanException {
        super(MiscConfigMBean.class);
    }

    public boolean isUseProxy() {
        return MyTunesRss.CONFIG.isProxyServer();
    }

    public void setUseProxy(boolean useProxy) {
        MyTunesRss.CONFIG.setProxyServer(useProxy);
        onChange();
    }

    public String getProxyHost() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getProxyHost());
    }

    public void setProxyHost(String proxyHost) {
        MyTunesRss.CONFIG.setProxyHost(StringUtils.trimToNull(proxyHost));
        onChange();
    }

    public int getProxyPort() {
        return MyTunesRss.CONFIG.getProxyPort();
    }

    public void setProxyPort(int port) {
        MyTunesRss.CONFIG.setProxyPort(port);
        onChange();
    }

    public String getMyTunesRssComUser() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getMyTunesRssComUser());
    }

    public void setMyTunesRssComUser(String usermame) {
        MyTunesRss.CONFIG.setMyTunesRssComUser(StringUtils.trimToNull(usermame));
        onChange();
    }

    public void setMyTunesRssComPassword(String password) throws UnsupportedEncodingException {
        if (StringUtils.isNotBlank(password)) {
            MyTunesRss.CONFIG.setMyTunesRssComPasswordHash(MyTunesRss.SHA1_DIGEST.digest(StringUtils.trimToEmpty(password).getBytes("UTF-8")));
            onChange();
        }
    }

    public boolean isMyTunesRssComSslSettings() {
        return MyTunesRss.CONFIG.isMyTunesRssComSsl();
    }

    public void setMyTunesRssComSslSettings(boolean ssl) {
        MyTunesRss.CONFIG.setMyTunesRssComSsl(ssl);
        onChange();
    }

    public String getWebWelcomeMessage() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebWelcomeMessage());
    }

    public void setWebWelcomeMessage(String message) {
        MyTunesRss.CONFIG.setWebWelcomeMessage(StringUtils.trimToNull(message));
        onChange();
    }

    public String getWebLoginMessage() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebLoginMessage());
    }

    public void setWebLoginMessage(String message) {
        MyTunesRss.CONFIG.setWebLoginMessage(StringUtils.trimToNull(message));
        onChange();
    }

    public String getMailHost() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getMailHost());
    }

    public void setMailHost(String mailHost) {
        MyTunesRss.CONFIG.setMailHost(StringUtils.trimToNull(mailHost));
        onChange();
    }

    public String getMailLogin() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getMailLogin());
    }

    public void setMailLogin(String mailLogin) {
        MyTunesRss.CONFIG.setMailLogin(StringUtils.trimToNull(mailLogin));
        onChange();
    }

    public void setMailPassword(String mailPassword) {
        if (StringUtils.isNotBlank(mailPassword)) {
            MyTunesRss.CONFIG.setMailPassword(StringUtils.trimToNull(mailPassword));
            onChange();
        }
    }

    public int getMailPort() {
        return MyTunesRss.CONFIG.getMailPort();
    }

    public void setMailPort(int mailPort) {
        MyTunesRss.CONFIG.setMailPort(mailPort);
        onChange();
    }

    public String getMailSender() {
        return StringUtils.trimToEmpty(MyTunesRss.CONFIG.getMailSender());
    }

    public void setMailSender(String mailSender) {
        MyTunesRss.CONFIG.setMailSender(StringUtils.trimToNull(mailSender));
        onChange();
    }

    public void setSmtpProtocol(String smtpProtocol) {
        MyTunesRss.CONFIG.setSmtpProtocol(SmtpProtocol.valueOf(smtpProtocol.toUpperCase(Locale.ENGLISH)));
        onChange();
    }

    public String getSmtpProtocol() {
        return MyTunesRss.CONFIG.getSmtpProtocol().name().toLowerCase(Locale.ENGLISH);
    }

    public boolean isMinimizeToSystray() {
        return MyTunesRss.CONFIG.isMinimizeToSystray();
    }

    public void setMinimizeToSystray(boolean minimizeToSystray) {
        MyTunesRss.CONFIG.setMinimizeToSystray(minimizeToSystray);
        onChange();
    }

    public boolean isServerBrowserActive() {
        return MyTunesRss.CONFIG.isServerBrowserActive();
    }

    public void setServerBrowserActive(boolean serverBrowserActive) {
        MyTunesRss.CONFIG.setServerBrowserActive(serverBrowserActive);
        onChange();
    }
}



