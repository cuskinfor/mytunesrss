package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.remotecontrol.RemoteTrackInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of the server local media player status.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MediaPlayerRepresentation implements RestRepresentation {

    private Integer myCurrentTime;
    private Integer myCurrentTrack;
    private Integer myLength;
    private Boolean myPlaying;
    private Integer myVolume;

    public MediaPlayerRepresentation() {
    }

    public MediaPlayerRepresentation(RemoteTrackInfo remoteTrackInfo) {
        setCurrentTime(remoteTrackInfo.getCurrentTime());
        setCurrentTrack(remoteTrackInfo.getCurrentTrack());
        setLength(remoteTrackInfo.getLength());
        setPlaying(remoteTrackInfo.isPlaying());
        setVolume(remoteTrackInfo.getVolume());
    }

    /**
     * Current time in seconds of the current track.
     */
    public Integer getCurrentTime() {
        return myCurrentTime;
    }

    public void setCurrentTime(Integer currentTime) {
        myCurrentTime = currentTime;
    }

    /**
     * Current track.
     */
    public Integer getCurrentTrack() {
        return myCurrentTrack;
    }

    public void setCurrentTrack(Integer currentTrack) {
        myCurrentTrack = currentTrack;
    }

    /**
     * Length of the current track in seconds.
     */
    public Integer getLength() {
        return myLength;
    }

    public void setLength(Integer length) {
        myLength = length;
    }

    /**
     * TRUE if currently a track is playing or FALSE otherwise (stopped or paused).
     */
    public Boolean isPlaying() {
        return myPlaying;
    }

    public void setPlaying(Boolean playing) {
        myPlaying = playing;
    }

    /**
     * The current volume (0 - 100).
     */
    public Integer getVolume() {
        return myVolume;
    }

    public void setVolume(Integer volume) {
        myVolume = volume;
    }
}
