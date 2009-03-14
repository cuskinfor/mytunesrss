package de.codewave.mytunesrss.jmx;

/**
 * de.codewave.mytunesrss.jmx.RemoteControlConfigMBean
 */
public interface RemoteControlConfigMBean {
    String getVideoLanClientHost();

    void setVideoLanClientHost(String host);

    int getVideoLanClientPort();

    void setVideoLanClientPort(int port);
}