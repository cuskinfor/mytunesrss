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

public class VlcPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VlcPlayer.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        MAPPER.getDeserializationConfig().setAnnotationIntrospector(introspector);
        MAPPER.getSerializationConfig().setAnnotationIntrospector(introspector);
        MAPPER.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private List<Track> myTracks = new ArrayList<Track>();

    private int myCurrent;

    private int myOffset = -1;

    private Thread myWatchdog;

    private String myVlcHost = "127.0.0.1";

    private int myVlcPort = 18675;

    private HttpClient myHttpClient;

    private String myRaopTarget;

    private BonjourServiceListener myRaopListener;

    private BonjourServiceListener myAirplayListener;

    public VlcPlayer(BonjourServiceListener raopListener, BonjourServiceListener airplayListener) {
        myRaopListener = raopListener;
        myAirplayListener = airplayListener;
    }

    public synchronized void init() throws VlcPlayerException {
        HttpResponseStatus status = new HttpResponseStatus();
        status.setFullscreen(0);
        status.setState("stopped");
        status.setTime(0);
        status.setPercentageVolume(70);
        init(status, -1);
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
                            if (StringUtils.isNotBlank(myRaopTarget)) {
                                command.add("--sout=#transcode{acodec=alac,channels=2,samplerate=44100}:raop{host=" + myRaopTarget + ",volume=128}");
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
                            semaphore.release();
                            if (process != null) {
                                process.destroy();
                                MyTunesRss.SPAWNED_PROCESSES.remove(process);
                            }
                        }
                    }
                }
            });
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

    public synchronized void setRaopTarget(String raopTarget) throws VlcPlayerException {
        raopTarget = StringUtils.trimToNull(raopTarget);
        if (!StringUtils.equalsIgnoreCase(myRaopTarget, raopTarget)) {
            HttpResponseStatus oldStatus = getStatus();
            destroy();
            myRaopTarget = raopTarget;
            init(oldStatus, myCurrent);
        }
    }

    public synchronized void shuffle() throws VlcPlayerException {
        stop();
        Collections.shuffle(myTracks);
        setTracks(myTracks);
    }

    public synchronized void clearPlaylist() throws VlcPlayerException {
        LOGGER.debug("Clearing playlist.");
        myTracks = Collections.emptyList();
        myCurrent = -1;
        myOffset = -1;
        send("/status.json?command=pl_empty");
    }

    public synchronized void setTracks(Track... tracks) throws VlcPlayerException {
        setTracks(Arrays.asList(tracks));
    }

    public synchronized void setTracks(List<Track> tracks) throws VlcPlayerException {
        clearPlaylist();
        LOGGER.debug("Setting playlist of " + tracks.size() + " tracks.");
        myTracks = new ArrayList<Track>(tracks);
        for (Track track : tracks) {
            send("/status.json?command=in_enqueue&input=" + MiscUtils.getUtf8UrlEncoded(track.getFile().getAbsolutePath()));
            if (myOffset == -1) {
                myOffset = getFirstTrackIndex();
            }
        }
    }

    private int getFirstTrackIndex() throws VlcPlayerException {
        int offset = 1; // default if no numbers are available
        HttpResponsePlaylist playlist = send("/playlist.json", HttpResponsePlaylist.class);
        if (playlist != null) {
            List<HttpResponsePlaylist> playlistTracks = getTracks(playlist);
            if (!playlistTracks.isEmpty()) {
                try {
                    offset = Math.max(offset, Integer.parseInt(playlistTracks.get(0).getId()));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        LOGGER.debug("First track has index " + offset);
        return offset;
    }

    public synchronized void addTrack(Track track) throws VlcPlayerException {
        addTracks(Collections.singletonList(track));
    }

    public synchronized void addTracks(Track... tracks) throws VlcPlayerException {
        addTracks(Arrays.asList(tracks));
    }

    public synchronized void addTracks(List<Track> tracks) throws VlcPlayerException {
        int oldSize = myTracks.size();
        if (oldSize == 0) {
            myOffset = -1;
        }
        myTracks.addAll(tracks);
        for (Track track : tracks) {
            send("/status.json?command=in_enqueue&input=" + MiscUtils.getUtf8UrlEncoded(track.getFile().getAbsolutePath()));
            if (myOffset == -1) {
                myOffset = getFirstTrackIndex();
            }
        }
        if (getStatus().isStopped()) {
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
        send("/status.json?command=volume&val=" + (volume * 512) / 100);
    }

    private synchronized void send(String command) throws VlcPlayerException {
        send(command, null);
    }

    private synchronized <T> T send(String command, Class<T> responseType) throws VlcPlayerException {
        LOGGER.debug("Sending command: " + command);
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
        setFullScreen(false);
        send("/status.json?command=pl_stop");
    }

    public synchronized void pause() throws VlcPlayerException {
        LOGGER.debug("Pausing playback.");
        send("/status.json?command=pl_forcepause");
    }

    public synchronized void seek(int percentage) throws VlcPlayerException {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be a value from 0 to 100 and was " + percentage);
        }
        LOGGER.debug("Seeking to " + percentage + "% of current track.");
        send("/status.json?command=seek&val=" + MiscUtils.getUtf8UrlEncoded(percentage + "%"));
    }

    public synchronized List<Track> getPlaylist() {
        return Collections.unmodifiableList(myTracks);
    }

    public synchronized int getCurrentIndex() {
        return myCurrent;
    }

    public synchronized HttpResponseStatus getStatus() throws VlcPlayerException {
        return send("/status.json", HttpResponseStatus.class);
    }

    public synchronized boolean setFullScreen(boolean fullScreen) throws VlcPlayerException {
        HttpResponseStatus status = send("/status.json", HttpResponseStatus.class);
        if (status != null && status.getFullscreen() == 0 && fullScreen) {
            LOGGER.debug("Switching to fullscreen mode.");
            send("/status.json?command=fullscreen");
        } else if (status != null && status.getFullscreen() == 1 && !fullScreen) {
            LOGGER.debug("Switching to window mode.");
            send("/status.json?command=fullscreen");
        }
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
            myCurrent = index;
            send("/status.json?command=pl_play&id=" + (myOffset + index));
        }
    }

    public Collection<BonjourDevice> getRaopDevices() {
        return myRaopListener.getDevices();
    }
}
