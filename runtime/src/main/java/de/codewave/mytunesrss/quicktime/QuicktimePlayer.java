package de.codewave.mytunesrss.quicktime;

import de.codewave.mytunesrss.datastore.statement.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.time.TaskAllMovies;
import quicktime.app.view.QTFactory;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.clocks.ExtremesCallBack;
import quicktime.std.clocks.TimeRecord;
import quicktime.std.movies.Movie;
import quicktime.std.movies.FullScreen;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.awt.*;

/**
 * de.codewave.mytunesrss.quicktime.QuicktimePlayer
 */
public class QuicktimePlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuicktimePlayer.class);

    private List<Track> myTracks = Collections.emptyList();

    private Movie myMovie;

    private int myCurrent;

    private boolean myPlaying;

    private boolean myInitialized;

    private JFrame myMovieFrame = new JFrame("MyTunesRSS Video");

    public synchronized void init() throws QuicktimePlayerException {
        if (!myInitialized) {
            LOGGER.debug("Initializing quicktime player.");
            try {
                QTSession.open();
                myInitialized = true;
                myMovieFrame.getContentPane().removeAll();
                myMovieFrame.getContentPane().add(new JLabel("init"));
                myMovieFrame.pack();
                myMovieFrame.setVisible(true);
                myMovieFrame.setVisible(false);
            } catch (QTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
    }

    public synchronized void destroy() throws QuicktimePlayerException {
        if (myInitialized) {
            stop();
            LOGGER.debug("Destroying quicktime player.");
            QTSession.exitMovies();
            QTSession.close();
            myInitialized = false;
        }
    }

    public synchronized void shuffle() throws QuicktimePlayerException {
        if (myMovie != null) {
            stop();
        }
        Collections.shuffle(myTracks);
    }

    public synchronized void clearPlaylist() throws QuicktimePlayerException {
        stop();
        LOGGER.debug("Clearing playlist.");
        myTracks = Collections.emptyList();
    }

    public synchronized void setTracks(List<Track> tracks) throws QuicktimePlayerException {
        clearPlaylist();
        LOGGER.debug("Setting playlist of " + tracks.size() + " tracks.");
        myTracks = new ArrayList<Track>(tracks);
    }

    public synchronized void setVolume(int volume) throws QuicktimePlayerException {
        LOGGER.debug("Setting volume to " + volume + "%.");
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be a value from 0 to 100 but was " + volume);
        }
        if (myMovie != null) {
            LOGGER.debug("Setting volume to " + volume + "%.");
            try {
                myMovie.setVolume((float) ((float) volume / 100.0));
            } catch (StdQTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
    }

    public synchronized int getVolume() throws QuicktimePlayerException {
        if (myMovie != null) {
            try {
                return (int) (myMovie.getVolume() * 100.0);
            } catch (StdQTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
        return -1;
    }

    public synchronized void stop() throws QuicktimePlayerException {
        try {
            if (myMovie != null) {
                LOGGER.debug("Stopping playback.");
                myMovie.stop();
                myMovieFrame.getContentPane().removeAll();
                myMovieFrame.setVisible(false);
                myMovie.setActive(false);
                myMovie = null;
                myPlaying = false;
                myCurrent = -1;
            }
        } catch (StdQTException e) {
            throw new QuicktimePlayerException(e);
        }
    }

    public synchronized void pause() throws QuicktimePlayerException {
        if (myMovie != null && isPlaying()) {
            try {
                LOGGER.debug("Pausing playback.");
                myMovie.stop();
                myPlaying = false;
            } catch (StdQTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
    }

    public synchronized void jumpTo(int percentage) throws QuicktimePlayerException {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be a value from 0 to 100 and was " + percentage);
        }
        LOGGER.debug("Jumping to position " + percentage + "% of current track.");
        try {
            if (myMovie != null) {
                int maxTime = myMovie.getDuration();
                long targetTime = (long) (((float) percentage * (float) maxTime) / 100.0);
                myMovie.setTime(new TimeRecord(myMovie.getTimeScale(), targetTime));
            }
        } catch (QTException e) {
            throw new QuicktimePlayerException(e);
        }
    }

    public synchronized List<Track> getPlaylist() {
        return Collections.unmodifiableList(myTracks);
    }

    public synchronized int getCurrentIndex() {
        return myCurrent;
    }

    public synchronized boolean isPlaying() {
        return myPlaying;
    }

    public synchronized int getCurrentTime() throws QuicktimePlayerException {
        if (myMovie != null) {
            try {
                return myMovie.getTime();
            } catch (StdQTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
        return -1;
    }

    public synchronized int getCurrentTrackLength() throws QuicktimePlayerException {
        try {
            if (myMovie != null) {
                return myMovie.getDuration();
            }
        } catch (StdQTException e) {
            throw new QuicktimePlayerException(e);
        }
        return -1;
    }

    public synchronized void play(int index) throws QuicktimePlayerException {
        LOGGER.debug("Playback of track " + index + " requested.");
        try {
            if (index >= 0 && index < myTracks.size()) {
                stop();
                Track track = myTracks.get(index);
                while (!track.getFile().isFile()) {
                    index++;
                    if (index >= myTracks.size()) {
                        return; // no more tracks
                    }
                    track = myTracks.get(index);
                }
                LOGGER.debug("Starting playback of track \"" + track.getName() + "\".");
                myMovie = Movie.fromFile(OpenMovieFile.asRead(new QTFile(track.getFile())));
                myCurrent = index;
                int width = myMovie.getBounds().getWidth();
                int height = myMovie.getBounds().getHeight();
                LOGGER.debug("Video dimension is " + width + " x " + height + ".");
                if (width != 0 && height != 0) {
                    myMovieFrame.getContentPane().removeAll();
                    myMovieFrame.getContentPane().add(QTFactory.makeQTComponent(myMovie).asComponent());
                    myMovieFrame.pack();
                    myMovieFrame.setVisible(true);
                }
                new QuicktimePlayerExtremesCallback(myMovie);
                TaskAllMovies.addMovieAndStart();
            } else if (index == -1 && myMovie != null) {
                LOGGER.debug("Continue playback of track \"" + myTracks.get(myCurrent).getName() + "\".");
            }
            myMovie.start();
            myPlaying = true;
        } catch (QTException
                e) {
            throw new QuicktimePlayerException(e);
        }
    }

    class QuicktimePlayerExtremesCallback extends ExtremesCallBack {
        QuicktimePlayerExtremesCallback(Movie movie) throws QTException {
            super(movie.getTimeBase(), StdQTConstants.triggerAtStop);
            callMeWhen();
        }

        public void execute() {
            LOGGER.debug("Track playback finished (callback executed).");
            try {
                myCurrent++;
                if (myCurrent >= myTracks.size()) {
                    LOGGER.debug("Reached end of track list, not playing anymore.");
                    stop();
                } else {
                    play(myCurrent);
                }
            } catch (QuicktimePlayerException e) {
                LOGGER.error("Could not start playback of next track.", e);
            }
        }
    }
}