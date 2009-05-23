package de.codewave.mytunesrss.remote.service;

/**
 * de.codewave.mytunesrss.remote.service.NoopRemoteController
 */
public class NoopRemoteController implements RemoteController {
    public void loadPlaylist(String playlistId, boolean start) {
        // intentionally left blank
    }

    public void loadAlbum(String albumName, boolean start) {
        // intentionally left blank
    }

    public void loadArtist(String artistName, boolean fullAlbums, boolean start) {
        // intentionally left blank
    }

    public void loadGenre(String genreName, boolean start) {
        // intentionally left blank
    }

    public void loadTrack(String trackId, boolean start) {
        // intentionally left blank
    }

    public void loadTracks(String[] trackIds, boolean start) {
        // intentionally left blank
    }

    public void clearPlaylist() {
        // intentionally left blank
    }

    public void play(int index) {
        // intentionally left blank
    }

    public void pause() {
        // intentionally left blank
    }

    public void stop() {
        // intentionally left blank
    }

    public void next() {
        // intentionally left blank
    }

    public void prev() {
        // intentionally left blank
    }

    public void jumpTo(int percentage) throws Exception {
        // intentionally left blank
    }

    public RemoteTrackInfo getCurrentTrackInfo() throws Exception {
        // intentionally left blank
        return null;
    }

    public void setVolume(int percentage) throws Exception {
        // intentionally left blank
    }

    public void setFullscreen(boolean fullscreen) throws Exception {
        // intentionally left blank
    }
}