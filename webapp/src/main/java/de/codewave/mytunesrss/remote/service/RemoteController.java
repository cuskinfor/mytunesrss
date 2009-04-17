package de.codewave.mytunesrss.remote.service;

import java.io.IOException;

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

    void play(int index) throws Exception;

    void pause() throws Exception;

    void stop() throws Exception;

    void next() throws Exception;

    void prev() throws Exception;
}