/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.vlc;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.utils.io.StreamCopyThread;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VlcPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VlcPlayer.class);
    private static final int TIMEOUT = 250;

    private List<Track> myTracks = Collections.emptyList();

    private int myCurrent;

    private int myOffset;

    private Thread myWatchdog;

    private String myVlcHost = "localhost";

    private int myVlcPort = 18675;

    public synchronized void init() throws VlcPlayerException {
        if (myWatchdog == null) {
            myWatchdog = new Thread(new Runnable() {
                public void run() {
                    ProcessBuilder processBuilder = new ProcessBuilder(
                            MyTunesRss.CONFIG.getVlcExecutable().getAbsolutePath(),
                            "--intf=rc",
                            "--rc-host=" + myVlcHost + ":" + myVlcPort
                    );
                    processBuilder.redirectErrorStream(true);
                    while (!Thread.interrupted()) {
                        LOGGER.debug("Initializing VLC player.");
                        Process process = null;
                        try {
                            process = processBuilder.start();
                            new StreamCopyThread(process.getInputStream(), false, new NullOutputStream(), true).start();
                            Thread.sleep(1000);
                            try {
                                setVolume(70); // default volume to 70%
                            } catch (VlcPlayerException e) {
                                LOGGER.warn("Could not set volume for VLC player.", e);
                            }
                            process.waitFor();
                        } catch (IOException e) {
                            LOGGER.warn("Could not start VLC player.", e);
                            break;
                        } catch (InterruptedException e) {
                            LOGGER.warn("Interrupted while waiting for process to exit.", e);
                            break;
                        } finally {
                            if (process != null) {
                                process.destroy();
                            }
                        }
                    }
                }
            });
            myWatchdog.setDaemon(true);
            myWatchdog.start();
        }
    }

    public synchronized void destroy() throws VlcPlayerException {
        if (myWatchdog != null && myWatchdog.isAlive()) {
            myWatchdog.interrupt();
            try {
                myWatchdog.join(5000);
            } catch (InterruptedException e) {
                throw new VlcPlayerException("Interrupted while waiting for watchdog thread to exit.", e);
            }
        }
    }

    public synchronized void shuffle() throws VlcPlayerException {
        stop();
        Collections.shuffle(myTracks);
        setTracks(myTracks);
    }

    public synchronized void clearPlaylist() throws VlcPlayerException {
        stop();
        LOGGER.debug("Clearing playlist.");
        myTracks = Collections.emptyList();
        myCurrent = -1;
        send("clear");
    }

    public synchronized void setTracks(List<Track> tracks) throws VlcPlayerException {
        stop();
        clearPlaylist();
        LOGGER.debug("Setting playlist of " + tracks.size() + " tracks.");
        myTracks = new ArrayList<Track>(tracks);
        myCurrent = -1;
        myOffset = -1;
        for (Track track : tracks) {
            send("enqueue " + track.getFile().getAbsolutePath());
            if (myOffset == -1) {
                for (String line : StringUtils.split(send("playlist"), "\r\n")) {
                    int digit = StringUtils.indexOfAny(line, "1234567890");
                    if (digit > -1 && digit < line.length() - 1) {
                        int nonDigit = line.indexOf(" ", digit);
                        if (nonDigit > digit) {
                            try {
                                int number = Integer.parseInt(line.substring(digit, nonDigit));
                                myOffset = Math.max(myOffset, number);
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                    }
                }
            }
        }
    }

    public synchronized void setVolume(int volume) throws VlcPlayerException {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be a value from 0 to 100 but was " + volume);
        }
        LOGGER.debug("Setting volume to " + volume + "%.");
        send("volume " + (volume * 512) / 100);
    }

    private String send(final String command) throws VlcPlayerException {
        try {
            LOGGER.debug("Sending command: " + command);
            Socket socket = new Socket(myVlcHost, myVlcPort);
            receive(socket.getInputStream());
            socket.getOutputStream().write((command + System.getProperty("line.separator")).getBytes());
            socket.getOutputStream().flush();
            String response = receive(socket.getInputStream());
            socket.getOutputStream().write(("logout" + System.getProperty("line.separator")).getBytes());
            socket.getOutputStream().flush();
            IOUtils.copy(socket.getInputStream(), new NullOutputStream());
            socket.close();
            LOGGER.debug("Command response: " + response);
            return response;
        } catch (IOException e) {
            throw new VlcPlayerException("Could not send command \"" + command + "\" to player.", e);
        }
    }

    private String receive(InputStream inputStream) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        byte[] buffer = new byte[8192];
        for (int read = inputStream.read(buffer); read > 0; read = inputStream.read(buffer)) {
            responseBuilder.append(new String(buffer, 0, read));
            if (responseBuilder.toString().endsWith("> ")) {
                responseBuilder.delete(responseBuilder.length() - 2, responseBuilder.length());
                break;
            }
        }
        LOGGER.trace("Received: " + responseBuilder);
        return StringUtils.trimToEmpty(responseBuilder.toString());
    }

    public synchronized int getVolume() throws VlcPlayerException {
        String response = send("volume");
        try {
            return (Integer.parseInt(response.trim()) * 100) / 512;
        } catch (NumberFormatException e) {
            LOGGER.info("Could not parse response \"" + response + "\" to current volume.");
            return -1;
        }
    }

    public synchronized void stop() throws VlcPlayerException {
        LOGGER.debug("Stopping playback.");
        setFullScreen(false);
        send("stop");
    }

    public synchronized void pause() throws VlcPlayerException {
        LOGGER.debug("Pausing playback.");
        send("pause");
    }

    public synchronized void jumpTo(int percentage) throws VlcPlayerException {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be a value from 0 to 100 and was " + percentage);
        }
        LOGGER.debug("Jumping to position " + percentage + "% of current track.");
        int length = getCurrentTrackLength();
        if (length > 0) {
            send("seek " + ((length * percentage) / 100));
        }
    }

    public synchronized List<Track> getPlaylist() {
        return Collections.unmodifiableList(myTracks);
    }

    public synchronized int getCurrentIndex() {
        return myCurrent;
    }

    public synchronized boolean isPlaying() throws VlcPlayerException {
        return send("status").contains("state playing");
    }

    public synchronized boolean isPaused() throws VlcPlayerException {
        return send("status").contains("state paused");
    }

    public synchronized boolean isStopped() throws VlcPlayerException {
        return send("status").contains("state stopped");
    }

    public synchronized int getCurrentTime() throws VlcPlayerException {
        String response = send("get_time");
        try {
            return Integer.parseInt(response);
        } catch (NumberFormatException e) {
            LOGGER.info("Could not parse response \"" + response + "\" to current time.");
            return -1;
        }
    }

    public synchronized int getCurrentTrackLength() throws VlcPlayerException {
        String response = send("get_length");
        try {
            return Integer.parseInt(response);
        } catch (NumberFormatException e) {
            LOGGER.info("Could not parse response \"" + response + "\" to current track length.");
            return -1;
        }
    }

    public synchronized boolean setFullScreen(boolean fullScreen) throws VlcPlayerException {
        LOGGER.debug("Switching to " + (fullScreen ? "fullscreen" : "window") + " mode.");
        send("fullscreen " + (fullScreen ? "on" : "off"));
        return fullScreen;
    }

    public synchronized void play(int index) throws VlcPlayerException {
        if (index < -1 || index >= myTracks.size()) {
            throw new IllegalArgumentException("Index out of track array bounds [0, " + (myTracks.size() - 1) + "]: " + index + ".");
        }
        LOGGER.debug("Playback of track " + index + " requested.");
        if (index == -1) {
            if (isPaused()) {
                send("pause");
            } else {
                send("play");
            }
        } else {
            myCurrent = index;
            send("goto " + (myOffset + index));
        }
    }
}
