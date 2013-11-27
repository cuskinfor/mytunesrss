/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.remotecontrol;

import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.Track;

import java.util.List;

public interface RemoteController {
    void loadPlaylist(User user, String playlistId) throws Exception;

    void loadAlbum(User user, String albumName, String albumArtistName) throws Exception;

    void loadArtist(User user, String artistName, boolean fullAlbums) throws Exception;

    void loadGenre(User user, String[] genreNames) throws Exception;

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
