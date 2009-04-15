package de.codewave.mytunesrss.jmx;

import de.codewave.mytunesrss.settings.RemoteControlType;

/**
 * de.codewave.mytunesrss.jmx.RemoteControlConfigMBean
 */
public interface RemoteControlConfigMBean {
    String getVideoLanClientHost();

    void setVideoLanClientHost(String host);

    int getVideoLanClientPort();

    void setVideoLanClientPort(int port);

    String getRemoteControlType();

    void disableRemoteControl();

    void setRemoteControlVideoLanClient();

    void setRemoteControlQuicktime();
}