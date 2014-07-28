/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediarenderercontrol;

public class MediaRendererTrackInfo {
    private int myCurrentTrack;
    private int myLength = -1;
    private int myCurrentTime = 0;
    private boolean myPlaying;
    private int myVolume;
    private String myMediaRendererId;
    private String myMediaRendererName;

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

    public int getVolume() {
        return myVolume;
    }

    public void setVolume(int volume) {
        myVolume = volume;
    }

    public String getMediaRendererId() {
        return myMediaRendererId;
    }

    public void setMediaRendererId(String mediaRendererId) {
        myMediaRendererId = mediaRendererId;
    }

    public String getMediaRendererName() {
        return myMediaRendererName;
    }

    public void setMediaRendererName(String mediaRendererName) {
        myMediaRendererName = mediaRendererName;
    }
}
