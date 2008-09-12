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
        return MyTunesRss.CONFIG.getProxyHost();
    }

    public void setProxyHost(String proxyHost) {
        MyTunesRss.CONFIG.setProxyHost(proxyHost);
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
        return MyTunesRss.CONFIG.getMyTunesRssComUser();
    }

    public void setMyTunesRssComUser(String usermame) {
        MyTunesRss.CONFIG.setMyTunesRssComUser(usermame);
        onChange();
    }

    public void setMyTunesRssComPassword(String password) throws UnsupportedEncodingException {
        MyTunesRss.CONFIG.setMyTunesRssComPasswordHash(MyTunesRss.SHA1_DIGEST.digest(StringUtils.trim(password).getBytes("UTF-8")));
        onChange();
    }

    public String getWebWelcomeMessage() {
        return MyTunesRss.CONFIG.getWebWelcomeMessage();
    }

    public void setWebWelcomeMessage(String message) {
        MyTunesRss.CONFIG.setWebWelcomeMessage(message);
        onChange();
    }

    public String getMailHost() {
        return MyTunesRss.CONFIG.getMailHost();
    }

    public void setMailHost(String mailHost) {
        MyTunesRss.CONFIG.setMailHost(mailHost);
        onChange();
    }

    public String getMailLogin() {
        return MyTunesRss.CONFIG.getMailLogin();
    }

    public void setMailLogin(String mailLogin) {
        MyTunesRss.CONFIG.setMailLogin(mailLogin);
        onChange();
    }

    public void setMailPassword(String mailPassword) {
        MyTunesRss.CONFIG.setMailPassword(mailPassword);
        onChange();
    }

    public int getMailPort() {
        return MyTunesRss.CONFIG.getMailPort();
    }

    public void setMailPort(int mailPort) {
        MyTunesRss.CONFIG.setMailPort(mailPort);
        onChange();
    }

    public String getMailSender() {
        return MyTunesRss.CONFIG.getMailSender();
    }

    public void setMailSender(String mailSender) {
        MyTunesRss.CONFIG.setMailSender(mailSender);
        onChange();
    }
}



