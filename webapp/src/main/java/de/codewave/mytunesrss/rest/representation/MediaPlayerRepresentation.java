package de.codewave.mytunesrss.rest.representation;

import de.codewave.mytunesrss.mediarenderercontrol.MediaRendererTrackInfo;
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
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
    private Long myPlaylistVersion;
    private String myMediaRendererId;
    private String myMediaRenderer;

    public MediaPlayerRepresentation() {
    }

    public MediaPlayerRepresentation(MediaRendererTrackInfo mediaRendererTrackInfo, long playlistVersion) {
        if (IncludeExcludeInterceptor.isAttr("currentTime")) {
            setCurrentTime(mediaRendererTrackInfo.getCurrentTime());
        }
        if (IncludeExcludeInterceptor.isAttr("currentTrack")) {
            setCurrentTrack(mediaRendererTrackInfo.getCurrentTrack());
        }
        if (IncludeExcludeInterceptor.isAttr("length")) {
            setLength(mediaRendererTrackInfo.getLength());
        }
        if (IncludeExcludeInterceptor.isAttr("playing")) {
            setPlaying(mediaRendererTrackInfo.isPlaying());
        }
        if (IncludeExcludeInterceptor.isAttr("volume")) {
            setVolume(mediaRendererTrackInfo.getVolume());
        }
        if (IncludeExcludeInterceptor.isAttr("playlistVersion")) {
            setPlaylistVersion(playlistVersion);
        }
        if (IncludeExcludeInterceptor.isAttr("mediaRenderer")) {
            setMediaRenderer(mediaRendererTrackInfo.getMediaRendererName());
        }
        if (IncludeExcludeInterceptor.isAttr("mediaRendererId")) {
            setMediaRendererId(mediaRendererTrackInfo.getMediaRendererId());
        }
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

    /**
     * Version of the playlist. Each modification to the playlist increments the version by one. So the client
     * can use the version to check for changes of the playlist and reload it in case the version has changed.
     */
    public Long getPlaylistVersion() {
        return myPlaylistVersion;
    }

    public void setPlaylistVersion(Long playlistVersion) {
        myPlaylistVersion = playlistVersion;
    }

    /**
     * The ID of the currently used media renderer.
     */
    public String getMediaRendererId() {
        return myMediaRendererId;
    }

    public void setMediaRendererId(String mediaRendererId) {
        myMediaRendererId = mediaRendererId;
    }

    /**
     * The name of the currently used media renderer.
     */
    public String getMediaRenderer() {
        return myMediaRenderer;
    }

    public void setMediaRenderer(String mediaRenderer) {
        myMediaRenderer = mediaRenderer;
    }
}
