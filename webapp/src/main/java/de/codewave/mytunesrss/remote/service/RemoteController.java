package de.codewave.mytunesrss.remote.service;

/**
 * de.codewave.mytunesrss.remote.service.RemoteControlService
 */
public interface RemoteController {
    void loadPlaylist(String playlistId, boolean start) throws Exception;

    void loadAlbum(String albumName, boolean start) throws Exception;

    void loadArtist(String artistName, boolean fullAlbums, boolean start) throws Exception;

    void loadGenre(String genreName, boolean start) throws Exception;

    void loadTrack(String trackId, boolean start) throws Exception;

    void loadTracks(String[] trackIds, boolean start) throws Exception;

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

    void jumpTo(int percentage) throws Exception;

    RemoteTrackInfo getCurrentTrackInfo() throws Exception;

    void setVolume(int percentage) throws Exception;
}