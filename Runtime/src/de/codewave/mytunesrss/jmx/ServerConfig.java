package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       daGama Business Travel GmbH<br>
 * <b>Creation Date:</b> 13.02.2007
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class ServerConfig implements ServerConfigMBean {
  public int getPort() {
    return MyTunesRss.CONFIG.getPort();
  }

  public void setPort(int port) {
    MyTunesRss.CONFIG.setPort(port);
  }

  public boolean isShowOnLocalNetwork() {
    return MyTunesRss.CONFIG.isAvailableOnLocalNet();
  }

  public void setShowOnLocalNetwork(boolean showOnLocalNetwork) {
    MyTunesRss.CONFIG.setAvailableOnLocalNet(showOnLocalNetwork);
  }

  public String getServerName() {
    return MyTunesRss.CONFIG.getServerName();
  }

  public void setServerName(String name) {
    MyTunesRss.CONFIG.setServerName(name);
  }

  public String getServerStatus() {
    throw new UnsupportedOperationException("Method  of class  is not yet implemented.");
  }

  public void startServer() {
    throw new UnsupportedOperationException("Method  of class  is not yet implemented.");
  }

  public void stopServer() {
    throw new UnsupportedOperationException("Method  of class  is not yet implemented.");
  }

  public boolean isAutostart() {
    return MyTunesRss.CONFIG.isAutoStartServer();
  }

  public void setAutostart(boolean autostart) {
    MyTunesRss.CONFIG.setAutoStartServer(autostart);
  }
}



