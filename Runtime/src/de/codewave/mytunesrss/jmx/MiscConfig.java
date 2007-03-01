package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import org.apache.commons.lang.StringUtils;

import javax.management.StandardMBean;
import javax.management.NotCompliantMBeanException;
import javax.management.MBeanAttributeInfo;
import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       Cologne Systems GmbH<br>
 * <b>Creation Date:</b> 01.03.2007
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
  }

  public String getProxyHost() {
    return MyTunesRss.CONFIG.getProxyHost();
  }

  public void setProxyHost(String proxyHost) {
    MyTunesRss.CONFIG.setProxyHost(proxyHost);
  }

  public int getProxyPort() {
    return MyTunesRss.CONFIG.getProxyPort();
  }

  public void setProxyPort(int port) {
    MyTunesRss.CONFIG.setProxyPort(port);
  }

  public String getMyTunesRssComUser() {
    return MyTunesRss.CONFIG.getMyTunesRssComUser();
  }

  public void setMyTunesRssComUser(String usermame) {
    MyTunesRss.CONFIG.setMyTunesRssComUser(usermame);
  }

  public void setMyTunesRssComPassword(String password) throws UnsupportedEncodingException {
    MyTunesRss.CONFIG.setMyTunesRssComPasswordHash(MyTunesRss.MESSAGE_DIGEST.digest(StringUtils.trim(password).getBytes("UTF-8")));
  }
}



