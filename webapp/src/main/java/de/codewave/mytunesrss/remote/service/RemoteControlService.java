package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.User;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;

/**
 * de.codewave.mytunesrss.remote.service.RemoteControlService
 */
public class RemoteControlService implements RemoteController {
    private RemoteController myRemoteController;

    private void assertAuthenticated() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user == null) {
            throw new IllegalAccessException("Unauthorized");
        }
    }

    private RemoteController getController() {
        switch (MyTunesRss.CONFIG.getRemoteControlType()) {
            case Vlc:
                return new VideoLanClientRemoteController();
            case Quicktime:
                return new QuicktimeRemoteController();
            default:
                return new NoopRemoteController();
        }
    }

    public void loadPlaylist(String playlistId, boolean start) throws Exception {
        assertAuthenticated();
        getController().loadPlaylist(playlistId, start);
    }

    public void loadAlbum(String albumName, boolean start) throws Exception {
        assertAuthenticated();
        getController().loadAlbum(albumName, start);
    }

    public void loadArtist(String artistName, boolean fullAlbums, boolean start) throws Exception {
        assertAuthenticated();
        getController().loadArtist(artistName, fullAlbums, start);
    }

    public void loadGenre(String genreName, boolean start) throws Exception {
        assertAuthenticated();
        getController().loadGenre(genreName, start);
    }

    public void loadTrack(String trackId, boolean start) throws Exception {
        assertAuthenticated();
        getController().loadTrack(trackId, start);
    }

    public void loadTracks(String[] trackIds, boolean start) throws Exception {
        assertAuthenticated();
        getController().loadTracks(trackIds, start);
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
}