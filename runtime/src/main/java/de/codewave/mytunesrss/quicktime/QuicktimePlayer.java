package de.codewave.mytunesrss.quicktime;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
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
import quicktime.std.movies.media.DataRef;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private JFrame myMovieFrame;

    public synchronized void init() throws QuicktimePlayerException {
        if (!myInitialized) {
            LOGGER.debug("Initializing quicktime player.");
            try {
                QTSession.open();
                myInitialized = true;
                createMovieFrame(false);
            } catch (QTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
    }

    private synchronized void createMovieFrame(boolean undecorated) {
        if (myMovieFrame != null) {
            myMovieFrame.dispose();
        }
        myMovieFrame = new JFrame("MyTunesRSS Video");
        myMovieFrame.setUndecorated(undecorated);
        myMovieFrame.setResizable(false);
        myMovieFrame.getContentPane().removeAll();
        myMovieFrame.getContentPane().add(new JLabel("init"));
        myMovieFrame.pack();
        myMovieFrame.setVisible(true);
        myMovieFrame.setVisible(false);
    }

    public synchronized void destroy() throws QuicktimePlayerException {
        if (myInitialized) {
            stop();
            myMovieFrame.dispose();
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
                setFullScreen(false);
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

    public synchronized boolean setFullScreen(boolean fullScreen) throws QuicktimePlayerException {
        LOGGER.debug("Switching to " + (fullScreen ? "fullscreen" : "window") + " mode.");
        try {
            int width = myMovie.getNaturalBoundsRect().getWidth();
            int height = myMovie.getNaturalBoundsRect().getHeight();
            LOGGER.debug("Video dimension is " + width + " x " + height + ".");
            if (width != 0 & height != 0) {
                if (fullScreen && myMovie != null) {
                    float rate = myMovie.getRate();
                    myMovie.stop();
                    createMovieFrame(true);
                    myMovieFrame.getContentPane().removeAll();
                    myMovieFrame.getContentPane().add(QTFactory.makeQTComponent(myMovie).asComponent());
                    myMovieFrame.pack();
                    myMovieFrame.setVisible(true);
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(myMovieFrame);
                    myMovie.setRate(rate);
                    return true;
                } else {
                    float rate = myMovie.getRate();
                    myMovie.stop();
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
                    createMovieFrame(false);
                    myMovieFrame.getContentPane().removeAll();
                    myMovie.setBounds(myMovie.getNaturalBoundsRect());
                    myMovieFrame.getContentPane().add(QTFactory.makeQTComponent(myMovie).asComponent());
                    myMovieFrame.pack();
                    myMovieFrame.setVisible(true);
                    myMovie.setRate(rate);
                }
            }
        } catch (QTException e) {
            throw new QuicktimePlayerException(e);
        }
        return false;
    }

    public synchronized void play(int index) throws QuicktimePlayerException {
        LOGGER.debug("Playback of track " + index + " requested.");
        try {
            if (index == -1 && myMovie == null) {
                // if continue playback is requested and no current movie, start playback of first track
                index = 0;
            }
            if (index >= 0 && index < myTracks.size()) {
                stop();
                Track track;
                for (track = myTracks.get(index), myMovie = getMovie(track); myMovie == null && index + 1 < myTracks.size(); index++, track = myTracks.get(index), myMovie = getMovie(track))
                    ;
                if (myMovie == null) {
                    return; // no more tracks to play
                }
                LOGGER.debug("Starting playback of track \"" + track.getName() + "\".");
                myCurrent = index;
                int width = myMovie.getNaturalBoundsRect().getWidth();
                int height = myMovie.getNaturalBoundsRect().getHeight();
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

    /**
     * Get a new movie instance from either the track's file or (for a you tube remote file) from the external
     * url.
     *
     * @param track The track.
     * @return The corresponding movie.
     * @throws QTException Any exception.
     */
    private Movie getMovie(Track track) {
        try {
            if (track.getSource() == TrackSource.YouTube) {
                return Movie.fromDataRef(new DataRef(MyTunesRssUtils.getYouTubeUrl(track.getId())), StdQTConstants.newMovieActive);
            } else {
                if (track.getFile().isFile()) {
                    return Movie.fromFile(OpenMovieFile.asRead(new QTFile(track.getFile())));
                }
            }
        } catch (QTException e) {
            LOGGER.error("Could not create movie from track \"" + track.getName() + "\".", e);
        }
        return null;
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