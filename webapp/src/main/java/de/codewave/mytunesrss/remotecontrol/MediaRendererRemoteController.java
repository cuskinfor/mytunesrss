package de.codewave.mytunesrss.remotecontrol;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.support.avtransport.callback.*;
import org.fourthline.cling.support.model.SeekMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaRendererRemoteController implements RemoteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaRendererRemoteController.class);
    private static final MediaRendererRemoteController INSTANCE = new MediaRendererRemoteController();

    public static final MediaRendererRemoteController getInstance() {
        return INSTANCE;
    }

    private MediaRendererRemoteController() {
        // only the singleton above can be created
    }

    // TODO thread safety
    private RemoteDevice myMediaRenderer;
    private List<Track> myTracks = new ArrayList<>();
    private int myCurrentTrack;

    public void loadPlaylist(User user, String playlistId) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = new FindPlaylistTracksQuery(user, playlistId, SortOrder.KeepOrder);
        loadItems(query);
    }

    private void loadItems(DataStoreQuery<QueryResult<Track>> query) throws SQLException {
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        setTracks(tracks);
    }

    public void setTracks(List<Track> tracks) {
        stop();
        myTracks.clear();
        myTracks.addAll(tracks);
    }

    private void addItems(DataStoreQuery<QueryResult<Track>> query, boolean startPlaybackIfStopped) throws SQLException {
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        int oldSize = myTracks.size();
        myTracks.addAll(tracks);
        if (!getCurrentTrackInfo().isPlaying()) {
            // start playback with first new track
            play(oldSize);
        }
    }

    public void loadAlbum(User user, String albumName, String albumArtistName) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForAlbum(user, new String[]{albumName}, StringUtils.isNotBlank(albumArtistName) ? new String[]{albumArtistName} : new String[0], SortOrder.Album);
        loadItems(query);
    }

    public void loadArtist(User user, String artistName, boolean fullAlbums) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForArtist(user, new String[]{artistName}, SortOrder.Album);
        loadItems(query);
    }

    public void loadGenre(User user, String genreName) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForGenre(user, new String[]{genreName}, SortOrder.Album);
        loadItems(query);
    }

    public void loadTracks(String[] trackIds) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForIds(trackIds);
        loadItems(query);
    }

    public void addTracks(String[] trackIds, boolean startPlaybackIfStopped) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForIds(trackIds);
        addItems(query, startPlaybackIfStopped);
    }

    public void clearPlaylist() {
        stop();
        myTracks.clear();
    }

    public void play(final int index) {
        if (myMediaRenderer != null) {
            // TODO handle index -1 as resume/start current
            final Track track = myTracks.get(index);
            final StringBuilder builder = createWebAppCall(MyTunesRss.CONFIG.getUser("mdescher"), "playTrack"); // TODO user for auth
            builder.append("/").append("track=").append(track.getId());
            MyTunesRss.UPNP_SERVICE.execute(new SetAVTransportURI(getAvTransport(), builder.toString(), track.getName()) {

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not set playback URL \"" + builder.toString() + "\" at media renderer \"" + myMediaRenderer.getDisplayString() + "\".");
                }

                @Override
                public void success(ActionInvocation invocation) {
                    myCurrentTrack = index;
                    MyTunesRss.UPNP_SERVICE.execute(new Play(getAvTransport()) {
                        @Override
                        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                            LOGGER.warn("Could not start playback on media renderer \"" + myMediaRenderer.getDetails().getFriendlyName() + "\".");
                        }
                    });
                }
            });
        }
    }

    public void pause() {
        if (myMediaRenderer != null) {
            MyTunesRss.UPNP_SERVICE.execute(new Pause(getAvTransport()) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not pause playback on media renderer \"" + myMediaRenderer.getDetails().getFriendlyName() + "\".");
                }
            });
        }
    }

    public void stop() {
        if (myMediaRenderer != null) {
            final RemoteDevice mediaRenderer = myMediaRenderer;
            final RemoteService service = mediaRenderer.findService(new UDAServiceId("AVTransport"));
            MyTunesRss.UPNP_SERVICE.execute(new Stop(service) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not stop playback on media renderer \"" + mediaRenderer.getDetails().getFriendlyName() + "\".");
                }
            });
        }
    }

    public void next() {
        stop();
        play(++myCurrentTrack);
    }

    public void prev() {
        stop();
        play(--myCurrentTrack);
    }

    public void seek(int percentage) {
        if (myMediaRenderer != null) {
            final String target = "H+:MM:SS"; // TODO
            MyTunesRss.UPNP_SERVICE.execute(new Seek(getAvTransport(), SeekMode.REL_TIME, target) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not seek to \"" + target + "\" on media renderer \"" + myMediaRenderer.getDetails().getFriendlyName() + "\".");
                }
            });
        }
    }

    public RemoteTrackInfo getCurrentTrackInfo() {
        // TODO get remote track info
        RemoteTrackInfo trackInfo = new RemoteTrackInfo();
        trackInfo.setCurrentTime(0);
        trackInfo.setCurrentTrack(myCurrentTrack + 1);
        trackInfo.setLength(0);
        trackInfo.setPlaying(false);
        trackInfo.setVolume(100);
        return trackInfo;
    }

    public void setVolume(int percentage) {
        // TODO set volume
    }

    public boolean setFullScreen(boolean fullScreen) {
        // TOOD set fullscreen
        return fullScreen;
    }

    public void shuffle() {
        stop();
        Collections.shuffle(myTracks);
        play(0);
    }

    public List<Track> getPlaylist() {
        return new ArrayList<>(myTracks);
    }

    public Track getTrack(int index) throws Exception {
        if (index < 0 || index >= myTracks.size()) {
            return null;
        }
        return myTracks.get(index);
    }

    @Override
    public void setAirtunesTargets(String[] airtunesTargets) throws Exception {
        throw new UnsupportedOperationException("Cannot set airtunes targets for the media renderer remote controller.");
    }

    public void setMediaRenderer(RemoteDevice mediaRenderer) throws Exception {
        stop();
        myMediaRenderer = mediaRenderer;
    }

    private RemoteService getAvTransport() {
        return myMediaRenderer != null ? myMediaRenderer.findService(new UDAServiceId("AVTransport")) : null;
    }

    private RemoteService getRenderingControl() {
        return myMediaRenderer != null ? myMediaRenderer.findService(new UDAServiceId("RenderingControl")) : null;
    }

    private StringBuilder createWebAppCall(User user, String command) {
        StringBuilder builder = new StringBuilder("http://");
        String hostAddress = myMediaRenderer.getIdentity().getDiscoveredOnLocalAddress().getHostAddress();
        builder.append(hostAddress).
                append(":").append(MyTunesRss.CONFIG.getPort());
        String context = StringUtils.trimToEmpty(MyTunesRss.CONFIG.getWebappContext());
        if (!context.startsWith("/")) {
            builder.append("/");
        }
        builder.append(context);
        if (context.length() > 0 && !context.endsWith("/")) {
            builder.append("/");
        }
        builder.append("mytunesrss/").
                append(command).
                append("/").
                append(MyTunesRssUtils.createAuthToken(user));
        return builder;
    }
}
