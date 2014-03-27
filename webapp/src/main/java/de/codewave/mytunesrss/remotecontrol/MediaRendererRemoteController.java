package de.codewave.mytunesrss.remotecontrol;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistTracksQuery;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.SortOrder;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.QueryResult;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.support.avtransport.callback.*;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MediaRendererRemoteController implements RemoteController {

    private static final class TrackWithUser {
        private final Track myTrack;
        private final User myUser;

        private TrackWithUser(Track track, User user) {
            myTrack = track;
            myUser = user;
        }

        public Track getTrack() {
            return myTrack;
        }

        public User getUser() {
            return myUser;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaRendererRemoteController.class);
    private static final MediaRendererRemoteController INSTANCE = new MediaRendererRemoteController();

    public static final MediaRendererRemoteController getInstance() {
        return INSTANCE;
    }

    private volatile RemoteDevice myMediaRenderer;
    private volatile SubscriptionCallback mySubscriptionCallback;
    private volatile List<TrackWithUser> myTracks = new ArrayList<>();
    private volatile int myCurrentTrack;
    private AtomicLong myMaxVolume = new AtomicLong(0);
    private volatile PositionInfo myPositionInfo;
    private AtomicInteger myVolume = new AtomicInteger(0);
    private AtomicBoolean myPlaying = new AtomicBoolean(false);

    private MediaRendererRemoteController() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(250);
                        final RemoteService avTransport = getAvTransport();
                        final RemoteService renderingControl = getRenderingControl();
                        if (avTransport != null && renderingControl != null) {
                            Future f1 = MyTunesRss.UPNP_SERVICE.execute(new GetPositionInfo(getAvTransport()) {
                                @Override
                                public void received(ActionInvocation invocation, PositionInfo positionInfo) {
                                    myPositionInfo = positionInfo;
                                }
                                @Override
                                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                                    LOGGER.warn("Could not get position info from media renderer \"" + avTransport.getDevice().getDetails().getFriendlyName() + "\".");
                                }
                            });
                            Future f2 = MyTunesRss.UPNP_SERVICE.execute(new GetVolume(getRenderingControl()) {
                                @Override
                                public void received(ActionInvocation actionInvocation, int currentVolume) {
                                    myVolume.set(currentVolume);
                                }
                                @Override
                                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                                    LOGGER.warn("Could not get volume from media renderer \"" + renderingControl.getDevice().getDetails().getFriendlyName() + "\".");
                                }
                            });
                            long start = System.currentTimeMillis();
                            try {
                                try {
                                    f1.get(1, TimeUnit.SECONDS);
                                } finally {
                                    f2.get(Math.max(1, 1000 - (System.currentTimeMillis() - start)), TimeUnit.MILLISECONDS);
                                }
                            } catch (ExecutionException | TimeoutException e) {
                                LOGGER.debug("Exception while waiting for future.", e);
                            }
                        }
                    }
                } catch (InterruptedException ignored) {
                    LOGGER.info("Media renderer query thread was interrupted and stops now.");
                }
            }
        }, "MediaRendererStatusQuery").start();
    }

    public synchronized void loadPlaylist(User user, String playlistId) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = new FindPlaylistTracksQuery(user, playlistId, SortOrder.KeepOrder);
        loadItems(user, query);
    }

    private void loadItems(User user, DataStoreQuery<QueryResult<Track>> query) throws SQLException {
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        setTracks(user, tracks);
    }

    public synchronized void setTracks(User user, List<Track> tracks) {
        stop();
        myTracks.clear();
        for (Track track : tracks) {
            myTracks.add(new TrackWithUser(track, user));
        }
    }

    private void addItems(User user, DataStoreQuery<QueryResult<Track>> query, boolean startPlaybackIfStopped) throws SQLException {
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        int oldSize = myTracks.size();
        for (Track track : tracks) {
            myTracks.add(new TrackWithUser(track, user));
        }
        if (!getCurrentTrackInfo().isPlaying() && startPlaybackIfStopped) {
            // start playback with first new track
            play(oldSize);
        }
    }

    public synchronized void loadAlbum(User user, String albumName, String albumArtistName) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForAlbum(user, new String[]{albumName}, StringUtils.isNotBlank(albumArtistName) ? new String[]{albumArtistName} : new String[0], SortOrder.Album);
        loadItems(user, query);
    }

    public synchronized void loadArtist(User user, String artistName, boolean fullAlbums) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForArtist(user, new String[]{artistName}, SortOrder.Album);
        loadItems(user, query);
    }

    public synchronized void loadGenre(User user, String genreName) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForGenre(user, new String[]{genreName}, SortOrder.Album);
        loadItems(user, query);
    }

    public synchronized void loadTracks(User user, String[] trackIds) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForIds(trackIds);
        loadItems(user, query);
    }

    public synchronized void addTracks(User user, String[] trackIds, boolean startPlaybackIfStopped) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForIds(trackIds);
        addItems(user, query, startPlaybackIfStopped);
    }

    public synchronized void clearPlaylist() {
        stop();
        myTracks.clear();
    }

    public synchronized void play(final int index) {
        final RemoteService service = getAvTransport();
        if (service != null) {
            // TODO handle index -1 as resume/start current
            final Track track = myTracks.get(index).getTrack();
            String hostAddress = service.getDevice().getIdentity().getDiscoveredOnLocalAddress().getHostAddress();
            final String playbackUrl = createPlaybackUrl(createBaseUrl(hostAddress, myTracks.get(index).getUser(), MyTunesRssCommand.PlayTrack.getName()), myTracks.get(index).getTrack());
            MyTunesRss.UPNP_SERVICE.execute(new SetAVTransportURI(service, playbackUrl, track.getName()) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not set playback URL \"" + playbackUrl + "\" at media renderer \"" + service.getDevice().getDisplayString() + "\".");
                }
                @Override
                public void success(ActionInvocation invocation) {
                    myCurrentTrack = index;
                    MyTunesRss.UPNP_SERVICE.execute(new Play(service) {
                        @Override
                        public void success(ActionInvocation invocation) {
                            myPlaying.set(true);
                        }

                        @Override
                        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                            LOGGER.warn("Could not start playback on media renderer \"" + service.getDevice().getDetails().getFriendlyName() + "\".");
                        }
                    });
                }
            });
        }
    }

    public synchronized void pause() {
        final RemoteService service = getAvTransport();
        if (service != null) {
            MyTunesRss.UPNP_SERVICE.execute(new Pause(service) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not pause playback on media renderer \"" + service.getDevice().getDetails().getFriendlyName() + "\".");
                }
            });
        }
    }

    public synchronized void stop() {
        final RemoteService service = getAvTransport();
        if (service != null) {
            MyTunesRss.UPNP_SERVICE.execute(new Stop(service) {
                @Override
                public void success(ActionInvocation invocation) {
                    myPlaying.set(false);
                }
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not stop playback on media renderer \"" + service.getDevice().getDetails().getFriendlyName() + "\".");
                }
            });
        }
    }

    public synchronized void next() {
        stop();
        play(++myCurrentTrack);
    }

    public synchronized void prev() {
        stop();
        play(--myCurrentTrack);
    }

    public synchronized void seek(int percentage) {
        final RemoteService service = getAvTransport();
        if (service != null) {
            final String target = "H+:MM:SS"; // TODO
            MyTunesRss.UPNP_SERVICE.execute(new Seek(service, SeekMode.REL_TIME, target) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not seek to \"" + target + "\" on media renderer \"" + service.getDevice().getDetails().getFriendlyName() + "\".");
                }
            });
        }
    }

    public synchronized RemoteTrackInfo getCurrentTrackInfo() {
        RemoteTrackInfo trackInfo = new RemoteTrackInfo();
        PositionInfo positionInfo = myPositionInfo;

        trackInfo.setCurrentTime(positionInfo != null ? (int)positionInfo.getTrackElapsedSeconds() : 0);
        trackInfo.setCurrentTrack(myCurrentTrack + 1);
        trackInfo.setLength(positionInfo != null ? (int)positionInfo.getTrackDurationSeconds() : 0);
        trackInfo.setPlaying(myPlaying.get());
        Long maxVolume = Math.max(myMaxVolume.get(), 1);
        trackInfo.setVolume(maxVolume != null ? (int)(((long)myVolume.get() * 100L) / maxVolume.longValue()) : 0);

        return trackInfo;
    }

    public synchronized void setVolume(final int percentage) {
        final Long maxVolume = myMaxVolume.get();
        final RemoteService service = getRenderingControl();
        if (service != null && maxVolume != null) {
            MyTunesRss.UPNP_SERVICE.execute(new SetVolume(service, ((long)percentage * maxVolume.longValue()) / 100L) {
                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not set volume to \"" + percentage + "\" on media renderer \"" + service.getDevice().getDetails().getFriendlyName() + "\".");
                }
            });
        }
    }

    public synchronized boolean setFullScreen(boolean fullScreen) {
        // TOOD set fullscreen
        return fullScreen;
    }

    public synchronized void shuffle() {
        stop();
        Collections.shuffle(myTracks);
        play(0);
    }

    public synchronized List<Track> getPlaylist() {
        List<Track> playlist = new ArrayList<>();
        for (TrackWithUser track : myTracks) {
            playlist.add(track.getTrack());
        }
        return playlist;
    }

    public synchronized Track getTrack(int index) throws Exception {
        if (index < 0 || index >= myTracks.size()) {
            return null;
        }
        return myTracks.get(index).getTrack();
    }

    @Override
    public synchronized void setAirtunesTargets(String[] airtunesTargets) throws Exception {
        throw new UnsupportedOperationException("Cannot set airtunes targets for the media renderer remote controller.");
    }

    public synchronized void setMediaRenderer(RemoteDevice mediaRenderer) throws Exception {
        if (mySubscriptionCallback != null) {
            mySubscriptionCallback.end();
        }
        stop();
        myMediaRenderer = mediaRenderer;
        mySubscriptionCallback = new AvTransportLastChangeSubscriptionCallback(getAvTransport()) {
            @Override
            void handleTransportStateChange(TransportState oldState, TransportState newState) {
                MediaRendererRemoteController.this.handleTransportStateChange(oldState, newState);
            }
        };
        MyTunesRss.UPNP_SERVICE.execute(mySubscriptionCallback);
        myMaxVolume.set(getRenderingControl().getStateVariable("Volume").getTypeDetails().getAllowedValueRange().getMaximum());
    }

    private synchronized void handleTransportStateChange(TransportState oldTransportState, TransportState newTransportState) {
        myPlaying.set(newTransportState == TransportState.PLAYING);
        if (newTransportState == TransportState.STOPPED && myCurrentTrack + 1 < myTracks.size()) {
            // advance if playback has stopped
            next();
        }
    }

    private RemoteService getAvTransport() {
        return myMediaRenderer != null ? myMediaRenderer.findService(new UDAServiceId("AVTransport")) : null;
    }

    private RemoteService getRenderingControl() {
        return myMediaRenderer != null ? myMediaRenderer.findService(new UDAServiceId("RenderingControl")) : null;
    }

    private String createPlaybackUrl(String baseUrl, Track track) {
        StringBuilder pathInfo = new StringBuilder("track=");
        pathInfo.append(MiscUtils.getUtf8UrlEncoded(track.getId()));
        TranscoderConfig transcoder = null;
        // TODO transcoders
        /*for (TranscoderConfig config : .................) {
            transcoder = MyTunesRssUtils.getTranscoder(config.getName(), track);
            if (transcoder != null) {
                pathInfo.append("/tc=").append(transcoder.getName());
            }
        }*/
        StringBuilder builder = new StringBuilder(StringUtils.stripEnd(baseUrl, "/"));
        builder.append("/").
                append(MyTunesRssUtils.encryptPathInfo(pathInfo.toString()));
        builder.append("/").
                append(MiscUtils.getUtf8UrlEncoded(MyTunesRssUtils.virtualTrackName(track))).
                append(".").
                append(MiscUtils.getUtf8UrlEncoded(transcoder != null ? transcoder.getTargetSuffix() : FilenameUtils.getExtension(track.getFilename())));
        return builder.toString();
    }

    private String createBaseUrl(String hostAddress, User user, String command) {
        StringBuilder builder = new StringBuilder("http://");
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
        return builder.toString();
    }
}
