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
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.support.avtransport.callback.*;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.TransportState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private MediaRendererRemoteController() {
        // only the singleton above can be created
    }

    // TODO thread safety
    private RemoteDevice myMediaRenderer;
    private SubscriptionCallback mySubscriptionCallback;
    private List<TrackWithUser> myTracks = new ArrayList<>();
    private int myCurrentTrack;

    public void loadPlaylist(User user, String playlistId) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = new FindPlaylistTracksQuery(user, playlistId, SortOrder.KeepOrder);
        loadItems(user, query);
    }

    private void loadItems(User user, DataStoreQuery<QueryResult<Track>> query) throws SQLException {
        List<Track> tracks = TransactionFilter.getTransaction().executeQuery(query).getResults();
        setTracks(user, tracks);
    }

    public void setTracks(User user, List<Track> tracks) {
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

    public void loadAlbum(User user, String albumName, String albumArtistName) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForAlbum(user, new String[]{albumName}, StringUtils.isNotBlank(albumArtistName) ? new String[]{albumArtistName} : new String[0], SortOrder.Album);
        loadItems(user, query);
    }

    public void loadArtist(User user, String artistName, boolean fullAlbums) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForArtist(user, new String[]{artistName}, SortOrder.Album);
        loadItems(user, query);
    }

    public void loadGenre(User user, String genreName) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForGenre(user, new String[]{genreName}, SortOrder.Album);
        loadItems(user, query);
    }

    public void loadTracks(User user, String[] trackIds) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForIds(trackIds);
        loadItems(user, query);
    }

    public void addTracks(User user, String[] trackIds, boolean startPlaybackIfStopped) throws SQLException {
        DataStoreQuery<QueryResult<Track>> query = FindTrackQuery.getForIds(trackIds);
        addItems(user, query, startPlaybackIfStopped);
    }

    public void clearPlaylist() {
        stop();
        myTracks.clear();
    }

    public void play(final int index) {
        if (myMediaRenderer != null) {
            // TODO handle index -1 as resume/start current
            final Track track = myTracks.get(index).getTrack();
            final String playbackUrl = createPlaybackUrl(createBaseUrl(myTracks.get(index).getUser(), MyTunesRssCommand.PlayTrack.getName()), myTracks.get(index).getTrack());
            MyTunesRss.UPNP_SERVICE.execute(new SetAVTransportURI(getAvTransport(), playbackUrl, track.getName()) {

                @Override
                public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                    LOGGER.warn("Could not set playback URL \"" + playbackUrl + "\" at media renderer \"" + myMediaRenderer.getDisplayString() + "\".");
                }

                @Override
                public void success(ActionInvocation invocation) {
                    myCurrentTrack = index;
                    startReceivingEvents();
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
            stopReceivingEvents();
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
            stopReceivingEvents();
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
        trackInfo.setCurrentTime(myMediaRenderer != null ? 0 : 0);
        trackInfo.setCurrentTrack(myCurrentTrack + 1);
        trackInfo.setLength(0);
        trackInfo.setPlaying(myMediaRenderer != null ? false : false);
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
        List<Track> playlist = new ArrayList<>();
        for (TrackWithUser track : myTracks) {
            playlist.add(track.getTrack());
        }
        return playlist;
    }

    public Track getTrack(int index) throws Exception {
        if (index < 0 || index >= myTracks.size()) {
            return null;
        }
        return myTracks.get(index).getTrack();
    }

    @Override
    public void setAirtunesTargets(String[] airtunesTargets) throws Exception {
        throw new UnsupportedOperationException("Cannot set airtunes targets for the media renderer remote controller.");
    }

    public void setMediaRenderer(RemoteDevice mediaRenderer) throws Exception {
        stop();
        myMediaRenderer = mediaRenderer;
    }

    private void stopReceivingEvents() {
        if (mySubscriptionCallback != null) {
            mySubscriptionCallback.end();
            mySubscriptionCallback = null;
        }
    }
    
    private void startReceivingEvents() {
        if (mySubscriptionCallback != null) {
            stopReceivingEvents();
        }
        mySubscriptionCallback = new SubscriptionCallback(getAvTransport()) {
            @Override
            protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception, String defaultMsg) {
                LOGGER.warn("AVTransport event subscription failed.", exception);
            }

            @Override
            protected void established(GENASubscription subscription) {
                LOGGER.info("AVTransport event subscription established.");
            }

            @Override
            protected void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
                LOGGER.info("AVTransport event subscription ended: \"" + reason + "\".");
            }

            @Override
            protected void eventReceived(GENASubscription subscription) {
                Map currentValues = subscription.getCurrentValues();
                if (currentValues.containsKey("LastChange")) {
                    String xml = currentValues.get("LastChange").toString();
                    try {
                        LastChange lastChange = new LastChange(new AVTransportLastChangeParser(), xml);
                        TransportState transportState = lastChange.getEventedValue(0, AVTransportVariable.TransportState.class).getValue();
                        String trackDuration = lastChange.getEventedValue(0, AVTransportVariable.CurrentTrackDuration.class).getValue();
                        URI trackUri = lastChange.getEventedValue(0, AVTransportVariable.AVTransportURI.class).getValue();
                        LOGGER.debug("LastChange: [transportState=" + transportState.name() + ", avTransportUri=" + trackUri + ", currentTrackDuration=" + trackDuration + "].");
                        if (transportState == TransportState.STOPPED) {
                            LOGGER.debug("Received LastChange event: PLAYBACK STOPPED.");
                            if (myCurrentTrack + 1 < myTracks.size()) {
                                next();
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.info("Could not parse LastChange event data (" + e.getClass().getSimpleName() + "): \"" + e.getMessage() + "\".");
                    }
                }
            }

            @Override
            protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
                LOGGER.info("AVTransport events missed: " + numberOfMissedEvents + ".");
            }
        };
        MyTunesRss.UPNP_SERVICE.execute(mySubscriptionCallback);
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
    
    private String createBaseUrl(User user, String command) {
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
        return builder.toString();
    }
}
