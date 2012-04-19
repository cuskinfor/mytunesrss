/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.vlc;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VlcPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(VlcPlayer.class);
    private static final int TIMEOUT = 250;

    private List<Track> myTracks = Collections.emptyList();

    private Process myVlcProcess;

    private int myCurrent;

    private boolean myInitialized;

    public synchronized void init() throws VlcPlayerException {
        if (!myInitialized) {
            LOGGER.debug("Initializing VLC player.");
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                        MyTunesRss.CONFIG.getVlcExecutable().getAbsolutePath(),
                        "--intf=rc"
                );
                myVlcProcess = processBuilder.start();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            IOUtils.copy(myVlcProcess.getErrorStream(), new NullOutputStream());
                        } catch (IOException e) {
                            LOGGER.error("Could not consume VLC process error stream.", e);
                        }
                    }
                }).start();
                receive(TIMEOUT);
                myInitialized = true;
                setVolume(70); // default volume to 70%
            } catch (IOException e) {
                throw new VlcPlayerException(e);
            }
        }
    }

    public synchronized void destroy() throws VlcPlayerException {
        if (myInitialized) {
            stop();
            LOGGER.debug("Destroying VLC player.");
            try {
                myVlcProcess.getOutputStream().write("shutdown\r\n".getBytes());
                receive(TIMEOUT);
            } catch (IOException e) {
                LOGGER.error("Could not shutdown VLC player gracefully.", e);
            } finally {
                myVlcProcess.destroy();
                myInitialized = false;
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
        sendAndReceive("clear");
    }

    public synchronized void setTracks(List<Track> tracks) throws VlcPlayerException {
        clearPlaylist();
        LOGGER.debug("Setting playlist of " + tracks.size() + " tracks.");
        myTracks = new ArrayList<Track>(tracks);
        for (Track track : tracks) {
            sendAndReceive("enqueue " + track.getFile().getAbsolutePath());
        }
    }

    public synchronized void setVolume(int volume) throws VlcPlayerException {
        LOGGER.debug("Setting volume to " + volume + "%.");
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be a value from 0 to 100 but was " + volume);
        }
        LOGGER.debug("Setting volume to " + volume + "%.");
        sendAndReceive("volume " + (volume * 512) / 100);
    }

    private String sendAndReceive(String s) throws VlcPlayerException {
        send(s);
        return receive(TIMEOUT);
    }

    private void send(String command) throws VlcPlayerException {
        try {
            LOGGER.debug("Sending: " + command);
            myVlcProcess.getOutputStream().write((command + System.getProperty("line.separator")).getBytes());
            myVlcProcess.getOutputStream().flush();
        } catch (IOException e) {
            throw new VlcPlayerException("Could not send command to player.", e);
        }
    }

    private String receive(long timeout) throws VlcPlayerException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Thread readerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    IOUtils.copy(myVlcProcess.getInputStream(), byteArrayOutputStream);
                } catch (IOException e) {
                    LOGGER.error("Could not read VLC process output.");
                }
            }
        });
        readerThread.setDaemon(true);
        LOGGER.debug("Reading response from VLC process.");
        readerThread.start();
        try {
            readerThread.join(timeout);
        } catch (InterruptedException e) {
            throw new VlcPlayerException("Interrupted while receiving response.", e);
        }
        readerThread.interrupt();
        String response = new String(byteArrayOutputStream.toByteArray());
        LOGGER.debug("Received: " + response);
        return response;
    }

    public synchronized int getVolume() throws VlcPlayerException {
        String response = sendAndReceive("volume");
        try {
            return (Integer.parseInt(response.trim()) * 100) / 512;
        } catch (NumberFormatException e) {
            LOGGER.warn("Could not parse volume response \"" + response + "\".");
            return -1;
        }
    }

    public synchronized void stop() throws VlcPlayerException {
        LOGGER.debug("Stopping playback.");
        setFullScreen(false);
        sendAndReceive("stop");
        myCurrent = -1;
    }

    public synchronized void pause() throws VlcPlayerException {
        LOGGER.debug("Pausing playback.");
        sendAndReceive("pause");
    }

    public synchronized void jumpTo(int percentage) throws VlcPlayerException {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be a value from 0 to 100 and was " + percentage);
        }
        LOGGER.debug("Jumping to position " + percentage + "% of current track.");
        int length = getCurrentTrackLength();
        if (length > 0) {
            sendAndReceive("seek " + ((length * percentage) / 100));
        }
    }

    public synchronized List<Track> getPlaylist() {
        return Collections.unmodifiableList(myTracks);
    }

    public synchronized int getCurrentIndex() {
        return myCurrent;
    }

    public synchronized boolean isPlaying() throws VlcPlayerException {
        for (String responseLine : StringUtils.split(sendAndReceive("status"), System.getProperty("line.separator"))) {
            if (responseLine.contains("state paused") || responseLine.contains("state stopped")) {
                return false;
            } else if (responseLine.contains("state playing")) {
                return true;
            }
        }
        throw new VlcPlayerException("Could not determine play state.");
    }

    public synchronized int getCurrentTime() throws VlcPlayerException {
        String response = sendAndReceive("get_time");
        try {
            return Integer.parseInt(response);
        } catch (NumberFormatException e) {
            throw new VlcPlayerException("Could not parse current time response.", e);
        }
    }

    public synchronized int getCurrentTrackLength() throws VlcPlayerException {
        String response = sendAndReceive("get_length");
        try {
            return Integer.parseInt(response);
        } catch (NumberFormatException e) {
            throw new VlcPlayerException("Could not parse length response \"" + response + "\".");
        }
    }

    public synchronized boolean setFullScreen(boolean fullScreen) throws VlcPlayerException {
        LOGGER.debug("Switching to " + (fullScreen ? "fullscreen" : "window") + " mode.");
        sendAndReceive("fullscreen " + (fullScreen ? "on" : "off"));
        return fullScreen;
    }

    public synchronized void play(int index) throws VlcPlayerException {
        LOGGER.debug("Playback of track " + index + " requested.");
        stop();
        sendAndReceive("goto " + index);
        sendAndReceive("play");
    }
}
