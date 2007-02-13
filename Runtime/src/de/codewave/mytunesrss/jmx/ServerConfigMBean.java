package de.codewave.mytunesrss.jmx;

/**
 * <b>Description:</b>   <br>
 * <b>Copyright:</b>     Copyright (c) 2007<br>
 * <b>Company:</b>       daGama Business Travel GmbH<br>
 * <b>Creation Date:</b> 13.02.2007
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
  String getServerStatus();
  void startServer();
  void stopServer();
  boolean isAutostart();
  void setAutostart(boolean autostart);
}



