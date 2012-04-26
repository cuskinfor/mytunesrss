package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jmdns.JmDnsDevice;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.render.RenderMachine;

import java.util.Collection;
import java.util.List;

/**
 * de.codewave.mytunesrss.remote.service.RemoteControlService
 */
public class RemoteControlService implements RemoteController {

    private void assertAuthenticated() throws IllegalAccessException {
        User user = MyTunesRssRemoteEnv.getSession().getUser();
        if (user == null || !user.isRemoteControl()) {
            throw new IllegalAccessException("UNAUTHORIZED");
        }
    }

    private RemoteController getController() {
        return MyTunesRss.VLC_PLAYER != null ? new VlcPlayerRemoteController() : new NoopRemoteController();
    }

    public void setAirtunesTarget(String airtunesTarget) throws Exception {
        assertAuthenticated();
        getController().setAirtunesTarget(airtunesTarget);
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

    public void addTrack(String trackId) throws Exception {
        assertAuthenticated();
        getController().addTrack(trackId);
    }

    public void addTracks(String[] trackIds) throws Exception {
        assertAuthenticated();
        getController().addTracks(trackIds);
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

    public void seek(int percentage) throws Exception {
        assertAuthenticated();
        getController().seek(percentage);
    }

    public RemoteTrackInfo getCurrentTrackInfo() throws Exception {
        assertAuthenticated();
        return getController().getCurrentTrackInfo();
    }

    public void setVolume(int percentage) throws Exception {
        assertAuthenticated();
        getController().setVolume(percentage);
    }

    public boolean setFullScreen(boolean fullScreen) throws Exception {
        assertAuthenticated();
        return getController().setFullScreen(fullScreen);
    }

    public void shuffle() throws Exception {
        assertAuthenticated();
        getController().shuffle();
    }

    public List<Track> getPlaylist() throws Exception {
        assertAuthenticated();
        return getController().getPlaylist();
    }

    public Track getTrack(int index) throws Exception {
        assertAuthenticated();
        return getController().getTrack(index);
    }

    public Object getRaopDevices() {
        Collection<JmDnsDevice> devices = MyTunesRss.RAOP_LISTENER.getDevices();
        return RenderMachine.getInstance().render(devices.toArray(new JmDnsDevice[devices.size()]));
    }

    public Object getAirplayDevices() {
        Collection<JmDnsDevice> devices = MyTunesRss.AIRPLAY_LISTENER.getDevices();
        return RenderMachine.getInstance().render(devices.toArray(new JmDnsDevice[devices.size()]));
    }
}