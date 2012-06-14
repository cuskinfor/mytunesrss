package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.remote.service.RemoteTrackInfo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MediaPlayerRepresentation extends RemoteTrackInfo {
    public MediaPlayerRepresentation(RemoteTrackInfo remoteTrackInfo) {
        setCurrentTime(remoteTrackInfo.getCurrentTime());
        setCurrentTrack(remoteTrackInfo.getCurrentTrack());
        setLength(remoteTrackInfo.getLength());
        setPlaying(remoteTrackInfo.isPlaying());
        setVolume(remoteTrackInfo.getVolume());
    }
}
