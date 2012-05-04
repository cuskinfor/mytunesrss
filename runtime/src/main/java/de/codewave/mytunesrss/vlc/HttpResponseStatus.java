/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.vlc;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.deser.std.StdDeserializer;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HttpResponseStatus {
    private boolean myFullscreen;
    private int myVolume;
    private VlcPlaybackState myState;
    private int myTime;
    private int myLength;

    public boolean isFullscreen() {
        return myFullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
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

    public VlcPlaybackState getState() {
        return myState;
    }

    public void setState(VlcPlaybackState state) {
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
        return getState() == VlcPlaybackState.playing;
    }

    public boolean isPaused() {
        return getState() == VlcPlaybackState.paused;
    }

    public boolean isStopped() {
        return getState() == VlcPlaybackState.stopped;
    }

    public void setPercentageVolume(int percentageVolume) {
        setVolume((512 * percentageVolume) / 100);
    }
}
