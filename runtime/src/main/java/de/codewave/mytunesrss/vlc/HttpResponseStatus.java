/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.vlc;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HttpResponseStatus {
    private int myFullscreen;
    private int myVolume;
    private String myState;
    private int myTime;
    private int myLength;

    public int getFullscreen() {
        return myFullscreen;
    }

    public boolean isFullscreen() {
        return getFullscreen() != 0;
    }

    public void setFullscreen(int fullscreen) {
        myFullscreen = fullscreen;
    }

    public int getVolume() {
        return myVolume;
    }

    public int getPercentageVolume() {
        return (getVolume() * 100) / 512;
    }

    public void setVolume(int volume) {
        myVolume = volume;
    }

    public String getState() {
        return myState;
    }

    public void setState(String state) {
        myState = state;
    }

    public int getTime() {
        return myTime;
    }

    public void setTime(int time) {
        myTime = time;
    }

    public int getLength() {
        return myLength;
    }

    public void setLength(int length) {
        myLength = length;
    }

    public boolean isPlaying() {
        return "playing".equalsIgnoreCase(getState());
    }

    public boolean isPaused() {
        return "paused".equalsIgnoreCase(getState());
    }

    public boolean isStopped() {
        return "stopped".equalsIgnoreCase(getState());
    }

    public void setPercentageVolume(int percentageVolume) {
        setVolume((512 * percentageVolume) / 100);
    }
}
