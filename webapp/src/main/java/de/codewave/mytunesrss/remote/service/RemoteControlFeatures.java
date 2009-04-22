package de.codewave.mytunesrss.remote.service;

/**
 * de.codewave.mytunesrss.remote.service.RemoteControlFeatures
 */
public class RemoteControlFeatures {
    private boolean myTimeInfo;
    private boolean myJumpTo;

    public RemoteControlFeatures(boolean timeInfo, boolean jumpTo) {
        myTimeInfo = timeInfo;
        myJumpTo = jumpTo;
    }

    public boolean isTimeInfo() {
        return myTimeInfo;
    }

    public boolean isJumpTo() {
        return myJumpTo;
    }
}