package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.datastore.statement.Track;

import java.util.List;

public interface RemoteController {
    void loadPlaylist(String playlistId) throws Exception;

    void loadAlbum(String albumName) throws Exception;

    void loadArtist(String artistName, boolean fullAlbums) throws Exception;

    void loadGenre(String genreName) throws Exception;

    void loadTracks(String[] trackIds) throws Exception;

    void addTracks(String[] trackIds, boolean startPlaybackIfStopped) throws Exception;

    void clearPlaylist() throws Exception;

    /**
     * Play a certain track of the current playlist.
     *
     * @param index The 1-based index of the track to play. A value of 0 should start playback at the current position (used to resume from a pause command).
     * @throws Exception Any exception from the service.
     */
    void play(int index) throws Exception;

    void pause() throws Exception;

    void stop() throws Exception;

    void next() throws Exception;

    void prev() throws Exception;

    void seek(int percentage) throws Exception;

    RemoteTrackInfo getCurrentTrackInfo() throws Exception;

    void setVolume(int percentage) throws Exception;

    boolean setFullScreen(boolean fullscreen) throws Exception;

    void shuffle() throws Exception;

    List<Track> getPlaylist() throws Exception;

    Track getTrack(int index) throws Exception;

    void setAirtunesTargets(String[] airtunesTargets) throws Exception;
}