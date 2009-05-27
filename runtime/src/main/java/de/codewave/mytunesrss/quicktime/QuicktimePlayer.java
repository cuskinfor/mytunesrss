package de.codewave.mytunesrss.quicktime;

import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quicktime.QTException;
import quicktime.QTSession;
import quicktime.app.time.TaskAllMovies;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.StdQTConstants;
import quicktime.std.StdQTException;
import quicktime.std.clocks.ExtremesCallBack;
import quicktime.std.clocks.TimeRecord;
import quicktime.std.movies.Movie;
import quicktime.std.movies.MovieController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.quicktime.QuicktimePlayer
 */
public class QuicktimePlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuicktimePlayer.class);

    private List<Track> myTracks = Collections.emptyList();

    private int myCurrent;

    private MovieController myMovieController;

    private boolean myPlaying;

    private boolean myInitialized;

    public synchronized void init() throws QuicktimePlayerException {
        if (!myInitialized) {
            LOGGER.debug("Initializing quicktime player.");
            try {
                QTSession.open();
                myInitialized = true;
            } catch (QTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
    }

    public synchronized void destroy() throws QuicktimePlayerException {
        if (myInitialized) {
            stop();
            LOGGER.debug("Destroying quicktime player.");
            if (SystemUtils.IS_OS_WINDOWS) {
                QTSession.exitMovies();
            } else {
                QTSession.close();
            }
            myInitialized = false;
        }
    }

    public synchronized void shuffle() throws QuicktimePlayerException {
        if (myMovieController != null) {
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
        if (myMovieController != null) {
            LOGGER.debug("Setting volume to " + volume + "%.");
            try {
                myMovieController.setVolume((float)((float) volume / 100.0));
            } catch (StdQTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
    }

    public synchronized int getVolume() throws QuicktimePlayerException {
        if (myMovieController != null) {
            try {
                return (int) (myMovieController.getVolume() * 100.0);
            } catch (StdQTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
        return -1;
    }

    public synchronized void stop() throws QuicktimePlayerException {
        try {
            if (myMovieController != null && myMovieController.getMovie() != null) {
                LOGGER.debug("Stopping playback.");
                myMovieController.play(0);
                myMovieController.removeMovie();
                myMovieController = null;
                myPlaying = false;
                myCurrent = -1;
            }
        } catch (StdQTException e) {
            throw new QuicktimePlayerException(e);
        }
    }

    public synchronized void pause() throws QuicktimePlayerException {
        if (myMovieController != null && isPlaying()) {
            try {
                LOGGER.debug("Pausing playback.");
                myMovieController.play(0);
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
            if (myMovieController != null && myMovieController.getMovie() != null) {
                int maxTime = myMovieController.getMovie().getDuration();
                long targetTime = (long) (((float) percentage * (float) maxTime) / 100.0);
                myMovieController.goToTime(new TimeRecord(myMovieController.getMovie().getTimeScale(), targetTime));
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
        if (myMovieController != null) {
            try {
                return myMovieController.getCurrentTime();
            } catch (StdQTException e) {
                throw new QuicktimePlayerException(e);
            }
        }
        return -1;
    }

    public synchronized int getCurrentTrackLength() throws QuicktimePlayerException {
        try {
            if (myMovieController != null && myMovieController.getMovie() != null) {
                return myMovieController.getMovie().getDuration();
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
                Track track = myTracks.get(index);
                stop();
                LOGGER.debug("Starting playback of track \"" + track.getName() + "\".");
                Movie movie = Movie.fromFile(OpenMovieFile.asRead(new QTFile(track.getFile())));
                new QuicktimePlayerExtremesCallback(movie);
                myMovieController = new MovieController(movie);
                TaskAllMovies.addMovieAndStart();
                myMovieController.play(1);
                myCurrent = index;
                myPlaying = true;
            } else if (index == -1 && myMovieController != null && myMovieController.getMovie() != null) {
                LOGGER.debug("Continue playback of track \"" + myTracks.get(myCurrent).getName() + "\".");
                myMovieController.play(1);
                myPlaying = true;
            }
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