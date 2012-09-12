/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.remotecontrol;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.mytunesrss.vlc.HttpResponseStatus;
import de.codewave.mytunesrss.vlc.VlcPlayerException;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class VlcPlayerRemoteController implements RemoteController {

    public void loadPlaylist(User user, String playlistId) throws SQLException, VlcPlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = new FindPlaylistTracksQuery(user, playlistId, SortOrder.KeepOrder);
        loadItems(query);
    }

    private void loadItems(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query) throws SQLException, VlcPlayerException {
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        MyTunesRss.VLC_PLAYER.setTracks(tracks);
    }

    private void addItems(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query, boolean startPlaybackIfStopped) throws SQLException, VlcPlayerException {
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        MyTunesRss.VLC_PLAYER.addTracks(tracks, startPlaybackIfStopped);
    }

    public void loadAlbum(User user, String albumName, String albumArtistName) throws SQLException, VlcPlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForAlbum(user, new String[]{albumName}, StringUtils.isNotBlank(albumArtistName) ? new String[]{albumArtistName} : new String[0], SortOrder.Album);
        loadItems(query);
    }

    public void loadArtist(User user, String artistName, boolean fullAlbums) throws SQLException, VlcPlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForArtist(user, new String[]{artistName}, SortOrder.Album);
        loadItems(query);
    }

    public void loadGenre(User user, String genreName) throws SQLException, VlcPlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForGenre(user, new String[]{genreName}, SortOrder.Album);
        loadItems(query);
    }

    public void loadTracks(String[] trackIds) throws SQLException, VlcPlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForIds(trackIds);
        loadItems(query);
    }

    public void addTracks(String[] trackIds, boolean startPlaybackIfStopped) throws SQLException, VlcPlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForIds(trackIds);
        addItems(query, startPlaybackIfStopped);
    }

    public void clearPlaylist() throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.clearPlaylist();
    }

    public void play(int index) throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.play(index);
    }

    public void pause() throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.pause();
    }

    public void stop() throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.stop();
    }

    public void next() throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.play(MyTunesRss.VLC_PLAYER.getCurrentIndex() + 1);
    }

    public void prev() throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.play(MyTunesRss.VLC_PLAYER.getCurrentIndex() - 1);
    }

    public void seek(int percentage) throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.seek(percentage);
    }

    public RemoteTrackInfo getCurrentTrackInfo() throws VlcPlayerException {
        RemoteTrackInfo trackInfo = new RemoteTrackInfo();
        HttpResponseStatus status = MyTunesRss.VLC_PLAYER.getStatus();
        trackInfo.setCurrentTime(status.getTime());
        trackInfo.setCurrentTrack(MyTunesRss.VLC_PLAYER.getCurrentIndex() + 1);
        trackInfo.setLength(status.getLength());
        trackInfo.setPlaying(status.isPlaying());
        trackInfo.setVolume(status.getPercentageVolume());
        return trackInfo;
    }

    public void setVolume(int percentage) throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.setVolume(percentage);
    }

    public boolean setFullScreen(boolean fullScreen) throws VlcPlayerException {
        return MyTunesRss.VLC_PLAYER.setFullScreen(fullScreen);
    }

    public void shuffle() throws VlcPlayerException {
        MyTunesRss.VLC_PLAYER.shuffle();
    }

    public List<Track> getPlaylist() {
        return MyTunesRss.VLC_PLAYER.getPlaylist();
    }

    public Track getTrack(int index) throws Exception {
        List<Track> tracks = MyTunesRss.VLC_PLAYER.getPlaylist();
        if (index < 0 || index >= tracks.size()) {
            return null;
        }
        return tracks.get(index);
    }

    public void setAirtunesTargets(String[] airtunesTargets) throws Exception {
        MyTunesRss.VLC_PLAYER.setRaopTargets(airtunesTargets);
    }
}