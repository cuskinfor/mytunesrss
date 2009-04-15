package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRssUtils;

public enum RemoteControlType {
    None(), Vlc(), Quicktime();

    @Override
    public String toString() {
        return MyTunesRssUtils.getBundleString("remotecontrol.type." + name());
    }

    public String getServiceName() {
        switch (this) {
            case Quicktime:
                return "QuicktimePlayerService";
            case Vlc:
                return "VideoLanClientService";
            default:
                return null;
        }
    }
}
