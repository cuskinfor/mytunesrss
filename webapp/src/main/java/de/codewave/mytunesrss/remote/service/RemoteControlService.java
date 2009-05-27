package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

/**
 * de.codewave.mytunesrss.remote.service.RemoteControlService
 */
public class RemoteControlService implements RemoteController {

    private void assertAuthenticated() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user == null) {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    private RemoteController getController() {
        return MyTunesRss.QUICKTIME_PLAYER != null ? new QuicktimePlayerRemoteController() : new NoopRemoteController();
//        switch (MyTunesRss.CONFIG.getRemoteControlType()) {
//            case Vlc:
//                return new VideoLanClientRemoteController();
//            case Quicktime:
//                return new QuicktimePlayerRemoteController();
//            default:
//                return new NoopRemoteController();
//        }
    }

    public void loadPlaylist(String playlistId) throws Exception {
        assertAuthenticated();
        getController().loadPlaylist(playlistId);
    }

    public void loadAlbum(String albumName) throws Exception {
        assertAuthenticated();
        getController().loadAlbum(albumName);
    }

    public void loadArtist(String artistName, boolean fullAlbums) throws Exception {
        assertAuthenticated();
        getController().loadArtist(artistName, fullAlbums);
    }

    public void loadGenre(String genreName) throws Exception {
        assertAuthenticated();
        getController().loadGenre(genreName);
    }

    public void loadTrack(String trackId) throws Exception {
        assertAuthenticated();
        getController().loadTrack(trackId);
    }

    public void loadTracks(String[] trackIds) throws Exception {
        assertAuthenticated();
        getController().loadTracks(trackIds);
    }

    public void clearPlaylist() throws Exception {
        assertAuthenticated();
        getController().clearPlaylist();
    }

    public void play(int index) throws Exception {
        assertAuthenticated();
        getController().play(index);
    }

    public void pause() throws Exception {
        assertAuthenticated();
        getController().pause();
    }

    public void stop() throws Exception {
        assertAuthenticated();
        getController().stop();
    }

    public void next() throws Exception {
        assertAuthenticated();
        getController().next();
    }

    public void prev() throws Exception {
        assertAuthenticated();
        getController().prev();
    }

    public void jumpTo(int percentage) throws Exception {
        assertAuthenticated();
        getController().jumpTo(percentage);
    }

    public RemoteTrackInfo getCurrentTrackInfo() throws Exception {
        assertAuthenticated();
        return getController().getCurrentTrackInfo();
    }

    public void setVolume(int percentage) throws Exception {
        assertAuthenticated();
        getController().setVolume(percentage);
    }

    public void setFullscreen(boolean fullscreen) throws Exception {
        assertAuthenticated();
        getController().setFullscreen(fullscreen);
    }

    public void shuffle() throws Exception {
        assertAuthenticated();
        getController().shuffle();
    }
}