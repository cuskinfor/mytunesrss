/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.vlc;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.bonjour.BonjourDevice;
import de.codewave.mytunesrss.bonjour.BonjourServiceListener;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.io.LogStreamCopyThread;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class VlcPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VlcPlayer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        MAPPER.getDeserializationConfig().setAnnotationIntrospector(introspector);
        MAPPER.getSerializationConfig().setAnnotationIntrospector(introspector);
        MAPPER.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static HttpResponseStatus newInitialStatus() {
        HttpResponseStatus status = new HttpResponseStatus();
        status.setFullscreen(false);
        status.setState(VlcPlaybackState.stopped);
        status.setTime(0);
        status.setPercentageVolume(70);
        return status;
    }

    public class StatusUpdater extends Thread {

        private volatile boolean myCancel;

        private volatile boolean myAdvanceListener;

        public StatusUpdater() {
            setDaemon(true);
            setName("VlcPlayerStatusUpdater");
        }

        public void run() {
            while (!myCancel) {
                try {
                    HttpResponseStatus status = send("/status.json", HttpResponseStatus.class);
                    VlcPlayer.this.myCurrentStatus.set(status);
                    if (myAdvanceListener && status.isStopped()) {
                        advance();
                    }
                } catch (VlcPlayerException e) {
                    LOGGER.info("Exception in status updater.", e);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // ignore and refresh status (this is what the interrupt signal is used for in this thread)
                }
            }
        }

        public void cancel() {
            myAdvanceListener = false;
            myCancel = true;
        }

        public void startAdvanceListener() {
            myAdvanceListener = true;
        }

        public void stopAdvanceListener() {
            myAdvanceListener = false;
        }
    }

    private List<Track> myTracks = new ArrayList<Track>();

    private int myCurrent;

    private Thread myWatchdog;

    private String myVlcHost = "127.0.0.1";

    private int myVlcPort;

    private HttpClient myHttpClient;

    private String[] myRaopTargets;

    private BonjourServiceListener myRaopListener;

    private BonjourServiceListener myAirplayListener;

    private AtomicReference<HttpResponseStatus> myCurrentStatus = new AtomicReference<HttpResponseStatus>(newInitialStatus());

    private StatusUpdater myStatusUpdater = new StatusUpdater();

    public VlcPlayer(BonjourServiceListener raopListener, BonjourServiceListener airplayListener) {
        myRaopListener = raopListener;
        myAirplayListener = airplayListener;
    }

    public synchronized void init() throws VlcPlayerException {
        init(newInitialStatus(), -1);
    }

    private void init(final HttpResponseStatus status, final int current) throws VlcPlayerException {
        if ((myWatchdog == null || !myWatchdog.isAlive() ) && MyTunesRss.CONFIG.getVlcExecutable() != null && MyTunesRss.CONFIG.getVlcExecutable().canExecute()) {
            final Semaphore semaphore = new Semaphore(0);
            myWatchdog = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.interrupted()) {
                        LOGGER.debug("Initializing VLC player.");
                        Process process = null;
                        try {
                            // lookup free port
                            ServerSocket serverSocket = new ServerSocket(0);
                            myVlcPort = serverSocket.getLocalPort();
                            serverSocket.close();
                            // process builder
                            List<String> command = new ArrayList<String>();
                            command.add(MyTunesRss.CONFIG.getVlcExecutable().getAbsolutePath());
                            command.add("--language=en");
                            command.add("--intf=http");
                            command.add("--http-host=" + myVlcHost);
                            command.add("--http-port=" + myVlcPort);
                            if (myRaopTargets != null && myRaopTargets.length > 0) {
                                command.add("--sout-keep");
                                if (myRaopTargets.length == 1) {
                                    command.add("--sout=#transcode{acodec=alac,channels=2,samplerate=44100}:gather:raop{host=" + myRaopTargets[0] + ",volume=" + ((255 * MyTunesRss.CONFIG.getVlcRaopVolume()) / 100) + "}");
                                } else {
                                    StringBuilder builder = new StringBuilder("--sout=#transcode{acodec=alac,channels=2,samplerate=44100}:duplicate{");
                                    for (int i = 0; i < myRaopTargets.length; i++) {
                                        if (StringUtils.isNotBlank(myRaopTargets[i])) {
                                            builder.append("dst=gather:raop{host=" + myRaopTargets[i] + ",volume=128}");
                                        } else {
                                            builder.append("dst=gather:display");
                                        }
                                        if (i + 1 < myRaopTargets.length) {
                                            builder.append(",");
                                        }
                                    }
                                    builder.append("}");
                                    command.add(builder.toString());
                                }
                            }
                            ProcessBuilder processBuilder = new ProcessBuilder(command);
                            processBuilder.redirectErrorStream(true);
                            LOGGER.info("Starting VLC player with HTTP interface on port " + myVlcPort + ".");
                            process = processBuilder.start();
                            MyTunesRss.SPAWNED_PROCESSES.add(process);
                            new LogStreamCopyThread(process.getInputStream(), false, LoggerFactory.getLogger(getClass()), LogStreamCopyThread.LogLevel.Debug).start();
                            myHttpClient = new HttpClient();
                            for (int i = 0; i < 10; i++) {
                                try {
                                    setVolume(status.getPercentageVolume());
                                    break;
                                } catch (VlcPlayerException e) {
                                    if (i == 9) {
                                        LOGGER.warn("Could not set volume for VLC player.", e);
                                    }
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ie) {
                                        Thread.currentThread().interrupt();
                                        break;
                                    }
                                }
                            }
                            if (!Thread.currentThread().isInterrupted() && myTracks != null && !myTracks.isEmpty()) {
                                try {
                                    setTracks(myTracks);
                                } catch (VlcPlayerException e) {
                                    LOGGER.warn("Could not set existing playlist again.", e);
                                }
                            }
                            myCurrent = current;
                            try {
                                setFullScreen(status.isFullscreen());
                                if (status.isPaused() || status.isPlaying()) {
                                    play(myCurrent);
                                    if (status.getLength() > 0 && status.getTime() > 0) {
                                        seek((status.getTime() * 100) / status.getLength());
                                    }
                                    pause();
                                }
                                if (status.isPlaying()) {
                                    play(-1);
                                }
                            } catch (VlcPlayerException e) {
                                LOGGER.warn("Could not restore old status.", e);
                            }
                            myStatusUpdater.start();
                            semaphore.release();
                            if (!Thread.currentThread().isInterrupted()) {
                                process.waitFor();
                            }
                        } catch (IOException e) {
                            LOGGER.warn("Could not start VLC player.", e);
                            break;
                        } catch (InterruptedException e) {
                            LOGGER.debug("Interrupted while waiting for process to exit.", e);
                            break;
                        } finally {
                            myStatusUpdater.cancel();
                            if (!Thread.currentThread().isInterrupted()) {
                                myStatusUpdater = new StatusUpdater();
                            }
                            semaphore.release();
                            if (process != null) {
                                process.destroy();
                                MyTunesRss.SPAWNED_PROCESSES.remove(process);
                            }
                        }
                    }
                }
            });
            myWatchdog.setName("VlcPlayerWatchdog");
            myWatchdog.setDaemon(true);
            myWatchdog.start();
            try {
                while (!semaphore.tryAcquire()) {
                    notifyAll();
                    wait(1000);
                }
            } catch (InterruptedException e) {
                throw new VlcPlayerException("Interrupted while waiting for VLC player to become initialized.", e);
            }
        }
    }

    public synchronized void destroy() throws VlcPlayerException {
        if (myWatchdog != null && myWatchdog.isAlive()) {
            LOGGER.info("Destroying VLC player.");
            myWatchdog.interrupt();
            try {
                myWatchdog.join(5000);
            } catch (InterruptedException e) {
                throw new VlcPlayerException("Interrupted while waiting for watchdog thread to exit.", e);
            }
            myWatchdog = null;
        }
    }

    public synchronized void setRaopTargets(String[] raopTargets) throws VlcPlayerException {
        Set<String> trimmedTargets = new HashSet<String>();
        for (String raopTarget : raopTargets) {
            trimmedTargets.add(StringUtils.trimToEmpty(raopTarget));
        }
        if (trimmedTargets.size() == 1 && StringUtils.isBlank(trimmedTargets.iterator().next())) {
            // only local playback
            trimmedTargets.clear();
        }
        if ((myRaopTargets == null && trimmedTargets != null && !trimmedTargets.isEmpty()) || (myRaopTargets != null && !CollectionUtils.isEqualCollection(Arrays.asList(myRaopTargets), trimmedTargets))) {
            HttpResponseStatus oldStatus = getStatus();
            destroy();
            myRaopTargets = trimmedTargets.toArray(new String[trimmedTargets.size()]);
            init(oldStatus, myCurrent);
        }
    }

    public synchronized void shuffle() throws VlcPlayerException {
        stop();
        Collections.shuffle(myTracks);
        setTracks(myTracks);
        play(0);
    }

    public synchronized void clearPlaylist() throws VlcPlayerException {
        LOGGER.debug("Clearing playlist.");
        stop();
        myTracks = Collections.emptyList();
        myCurrent = -1;
        send("/status.json?command=pl_empty");
    }

    public synchronized void setTracks(Track... tracks) throws VlcPlayerException {
        setTracks(Arrays.asList(tracks));
    }

    public synchronized void setTracks(List<Track> tracks) throws VlcPlayerException {
        clearPlaylist();
        LOGGER.debug("Setting playlist of " + tracks.size() + " tracks.");
        myTracks = new ArrayList<Track>(tracks);
    }

    public synchronized void addTracks(List<Track> tracks, boolean startPlaybackIfStopped) throws VlcPlayerException {
        int oldSize = myTracks.size();
        myTracks.addAll(tracks);
        if (startPlaybackIfStopped && getStatus().isStopped()) {
            // start playback of first new track if currently in stopped mode
            play(oldSize);
        }
    }

    private List<HttpResponsePlaylist> getTracks(HttpResponsePlaylist playlist) {
        if ("Playlist".equals(playlist.getName())) {
            return playlist.getChildren();
        } else if (playlist.getChildren() != null) {
            for (HttpResponsePlaylist child : playlist.getChildren()) {
                List<HttpResponsePlaylist> tracks = getTracks(child);
                if (!tracks.isEmpty()) {
                    return tracks;
                }
            }
        }
        return new ArrayList<HttpResponsePlaylist>();
    }

    public synchronized void setVolume(int volume) throws VlcPlayerException {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be a value from 0 to 100 but was " + volume);
        }
        LOGGER.debug("Setting volume to " + volume + "%.");
        int newVolumeValue = (volume * 512) / 100;
        send("/status.json?command=volume&val=" + newVolumeValue);
        myStatusUpdater.interrupt(); // trigger an immediate update
    }

    private synchronized void send(String command) throws VlcPlayerException {
        send(command, null);
    }

    private synchronized <T> T send(String command, Class<T> responseType) throws VlcPlayerException {
        LOGGER.trace("Sending command: " + command);
        GetMethod getMethod = new GetMethod("http://" + myVlcHost + ":" + myVlcPort + "/requests" + command);
        try {
            getMethod.getParams().setSoTimeout(MyTunesRss.CONFIG.getVlcSocketTimeout());
            if (myHttpClient.executeMethod(getMethod) == 200 && responseType != null) {
                return MAPPER.readValue(getMethod.getResponseBodyAsStream(), responseType);
            }
        } catch (IOException e) {
            throw new VlcPlayerException("Could not send command \"" + command + "\" to player.", e);
        } finally {
            getMethod.releaseConnection();
        }
        return null;
    }

    public synchronized void stop() throws VlcPlayerException {
        LOGGER.debug("Stopping playback.");
        myStatusUpdater.stopAdvanceListener();
        setFullScreen(false);
        send("/status.json?command=pl_stop");
        myStatusUpdater.interrupt(); // trigger an immediate update
    }

    public synchronized void pause() throws VlcPlayerException {
        LOGGER.debug("Pausing playback.");
        myStatusUpdater.stopAdvanceListener();
        send("/status.json?command=pl_forcepause");
        myStatusUpdater.interrupt(); // trigger an immediate update
    }

    public synchronized void seek(int percentage) throws VlcPlayerException {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be a value from 0 to 100 and was " + percentage);
        }
        LOGGER.debug("Seeking to " + percentage + "% of current track.");
        send("/status.json?command=seek&val=" + MiscUtils.getUtf8UrlEncoded(percentage + "%"));
        myStatusUpdater.interrupt(); // trigger an immediate update
    }

    public synchronized List<Track> getPlaylist() {
        return Collections.unmodifiableList(myTracks);
    }

    public synchronized int getCurrentIndex() {
        return myCurrent;
    }

    public HttpResponseStatus getStatus() throws VlcPlayerException {
        return myCurrentStatus.get();
    }

    public synchronized boolean setFullScreen(boolean fullScreen) throws VlcPlayerException {
        HttpResponseStatus status = send("/status.json", HttpResponseStatus.class);
        if (status != null && !status.isFullscreen() && fullScreen) {
            LOGGER.debug("Switching to fullscreen mode.");
            send("/status.json?command=fullscreen");
        } else if (status != null && status.isFullscreen() && !fullScreen) {
            LOGGER.debug("Switching to window mode.");
            send("/status.json?command=fullscreen");
        }
        myStatusUpdater.interrupt(); // trigger an immediate update
        return fullScreen;
    }

    public synchronized void play(int index) throws VlcPlayerException {
        if (index < -1 || index >= myTracks.size()) {
            throw new IllegalArgumentException("Index out of track array bounds [0, " + (myTracks.size() - 1) + "]: " + index + ".");
        }
        LOGGER.debug("Playback of track " + index + " requested.");
        if (index == -1) {
            if (getStatus().isPaused()) {
                send("/status.json?command=pl_forceresume");
            } else {
                play(myCurrent);
            }
        } else {
            myStatusUpdater.stopAdvanceListener();
            myCurrent = index;
            send("/status.json?command=pl_empty");
            send("/status.json?command=in_play&input=" + MiscUtils.getUtf8UrlEncoded(myTracks.get(index).getFile().getAbsolutePath()));
        }
        myStatusUpdater.startAdvanceListener();
        myStatusUpdater.interrupt();
    }

    private synchronized void advance() throws VlcPlayerException {
        if (myCurrent + 1 < myTracks.size()) {
            play(myCurrent + 1);
        }
    }

    public Collection<BonjourDevice> getRaopDevices() {
        return myRaopListener.getDevices();
    }

    private synchronized void removeTrack(int index) throws VlcPlayerException {
        if (index < 0 || index >= myTracks.size()) {
            throw new IllegalArgumentException("Illegal index \"" + index + "\" for list of \"" + myTracks.size() + "\" tracks.");
        }
        if (myCurrent == index) {
            stop();
        }
        myTracks.remove(index);
    }
}
