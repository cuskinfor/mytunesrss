package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.settings.ServerInfo;

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

  public boolean isRunning() {
    return MyTunesRss.WEBSERVER.isRunning();
  }

  public String startServer() {
      MyTunesRss.ERROR_QUEUE.clear();
      if (!MyTunesRss.WEBSERVER.isRunning()) {
          MyTunesRss.startWebserver();
      }
      return MyTunesRss.ERROR_QUEUE.popLastError();
  }

  public String stopServer() {
      MyTunesRss.ERROR_QUEUE.clear();
      if (MyTunesRss.WEBSERVER.isRunning()) {
          MyTunesRss.stopWebserver();
      }
      return MyTunesRss.ERROR_QUEUE.popLastError();
  }

  public boolean isAutostart() {
    return MyTunesRss.CONFIG.isAutoStartServer();
  }

  public void setAutostart(boolean autostart) {
    MyTunesRss.CONFIG.setAutoStartServer(autostart);
  }

    public boolean isCreateTempArchives() {
        return MyTunesRss.CONFIG.isLocalTempArchive();
    }

    public void setCreateTempArchives(boolean createTempArchives) {
        MyTunesRss.CONFIG.setLocalTempArchive(createTempArchives);
    }

  public String getExternalAddress() {
    return ServerInfo.getExternalAddress(Integer.toString(getPort()));
  }

  public Object getLocalAddresses() {
    String[] addresses = ServerInfo.getLocalAddresses(Integer.toString(getPort()));
    return addresses.length == 1 ? addresses[0] : addresses;
  }
}



