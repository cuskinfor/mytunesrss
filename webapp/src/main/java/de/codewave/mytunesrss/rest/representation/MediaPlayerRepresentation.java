package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.remote.service.RemoteTrackInfo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MediaPlayerRepresentation {

    private int myCurrentTime;
    private int myCurrentTrack;
    private int myLength;
    private boolean myPlaying;
    private int myVolume;

    public MediaPlayerRepresentation() {
    }

    public MediaPlayerRepresentation(RemoteTrackInfo remoteTrackInfo) {
        setCurrentTime(remoteTrackInfo.getCurrentTime());
        setCurrentTrack(remoteTrackInfo.getCurrentTrack());
        setLength(remoteTrackInfo.getLength());
        setPlaying(remoteTrackInfo.isPlaying());
        setVolume(remoteTrackInfo.getVolume());
    }

    public int getCurrentTime() {
        return myCurrentTime;
    }

    public void setCurrentTime(int currentTime) {
        myCurrentTime = currentTime;
    }

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
}
