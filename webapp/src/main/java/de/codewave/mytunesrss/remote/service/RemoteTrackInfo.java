package de.codewave.mytunesrss.remote.service;

import org.apache.commons.lang.StringUtils;

/**
 * de.codewave.mytunesrss.remote.service.RemoteTrackInfo
 */
public class RemoteTrackInfo {
    private int myCurrentTrack;
    private int myLength = -1;
    private int myCurrentTime = 0;
    private boolean myPlaying;

    public int getCurrentTrack() {
        return myCurrentTrack;
    }

    public void setCurrentTrack(int currentTrack) {
        myCurrentTrack = currentTrack;
    }

    public int getLength() {
        return myLength;
    }

    public void setLength(int length) {
        myLength = length;
    }

    public int getCurrentTime() {
        return myCurrentTime;
    }

    public void setCurrentTime(int currentTime) {
        myCurrentTime = currentTime;
    }

    public boolean isPlaying() {
        return myPlaying;
    }

    public void setPlaying(boolean playing) {
        myPlaying = playing;
    }
}