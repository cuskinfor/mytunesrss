package de.codewave.mytunesrss.remote.service;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.quicktime.QuicktimePlayerException;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;

import java.sql.SQLException;
import java.util.List;

public class QuicktimePlayerRemoteController implements RemoteController {

    public void loadPlaylist(String playlistId) throws SQLException, QuicktimePlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = new FindPlaylistTracksQuery(playlistId, FindPlaylistTracksQuery.SortOrder.KeepOrder);
        loadItems(query);
    }

    private void loadItems(DataStoreQuery<DataStoreQuery.QueryResult<Track>> query) throws SQLException, QuicktimePlayerException {
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        MyTunesRss.QUICKTIME_PLAYER.setTracks(tracks);
    }

    public void loadAlbum(String albumName) throws SQLException, QuicktimePlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForAlbum(MyTunesRssRemoteEnv.getSession().getUser(), new String[]{albumName}, false);
        loadItems(query);
    }

    public void loadArtist(String artistName, boolean fullAlbums) throws SQLException, QuicktimePlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForArtist(MyTunesRssRemoteEnv.getSession().getUser(), new String[]{artistName}, false);
        loadItems(query);
    }

    public void loadGenre(String genreName) throws SQLException, QuicktimePlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForGenre(MyTunesRssRemoteEnv.getSession().getUser(), new String[]{genreName}, false);
        loadItems(query);
    }

    public void loadTrack(String trackId) throws SQLException, QuicktimePlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForId(new String[]{trackId});
        loadItems(query);
    }

    public void loadTracks(String[] trackIds) throws SQLException, QuicktimePlayerException {
        DataStoreQuery<DataStoreQuery.QueryResult<Track>> query = FindTrackQuery.getForId(trackIds);
        loadItems(query);
    }

    public void clearPlaylist() throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.clearPlaylist();
    }

    public void play(int index) throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.play(index - 1);
    }

    public void pause() throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.pause();
    }

    public void stop() throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.stop();
    }

    public void next() throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.play(MyTunesRss.QUICKTIME_PLAYER.getCurrentIndex() + 1);
    }

    public void prev() throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.play(MyTunesRss.QUICKTIME_PLAYER.getCurrentIndex() - 1);
    }

    public void jumpTo(int percentage) throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.jumpTo(percentage);
    }

    public RemoteTrackInfo getCurrentTrackInfo() throws QuicktimePlayerException {
        RemoteTrackInfo trackInfo = new RemoteTrackInfo();
        trackInfo.setCurrentTime(MyTunesRss.QUICKTIME_PLAYER.getCurrentTime());
        trackInfo.setCurrentTrack(MyTunesRss.QUICKTIME_PLAYER.getCurrentIndex() + 1);
        trackInfo.setLength(MyTunesRss.QUICKTIME_PLAYER.getCurrentTrackLength());
        trackInfo.setPlaying(MyTunesRss.QUICKTIME_PLAYER.isPlaying());
        trackInfo.setVolume(MyTunesRss.QUICKTIME_PLAYER.getVolume());
        return trackInfo;
    }

    public void setVolume(int percentage) throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.setVolume(percentage);
    }

    public void showVideo(boolean show, boolean fullscreen) throws QuicktimePlayerException {
        // todo: implement method
        throw new UnsupportedOperationException("method showVideo of class QuicktimePlayerRemoteController is not yet implemented!");
    }

    public void shuffle() throws QuicktimePlayerException {
        MyTunesRss.QUICKTIME_PLAYER.shuffle();
    }
}