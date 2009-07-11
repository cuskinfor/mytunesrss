package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.lang.StringUtils;

import javax.management.NotCompliantMBeanException;
import java.io.UnsupportedEncodingException;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2007<br> <b>Company:</b>       Cologne Systems GmbH<br> <b>Creation Date:</b>
 * 01.03.2007
 *
 * @author Michael Descher
 * @version 1.0
 */
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

    public boolean isMailTls() {
        return MyTunesRss.CONFIG.isMailTls();
    }

    public void setMailTls(boolean mailTls) {
        MyTunesRss.CONFIG.setMailTls(mailTls);
        onChange();
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



