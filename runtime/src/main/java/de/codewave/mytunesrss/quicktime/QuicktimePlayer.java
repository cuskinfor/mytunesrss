package de.codewave.mytunesrss.quicktime;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import de.codewave.utils.swing.SwingUtils;
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

    private QuicktimePlayerExtremesCallback myCallback;

    private int myCurrent;

    private int myCurrentVolume;

    private boolean myPlaying;

    private boolean myInitialized;

    private JFrame myMovieFrame = new JFrame();

    public synchronized void init() throws QuicktimePlayerException {
        if (!myInitialized) {
            LOGGER.debug("Initializing quicktime player.");
            try {
                QTSession.open();
                myInitialized = true;
                setVolume(70); // default volume to 70%
            } catch (QTException e) {
                throw new QuicktimePlayerException(e);
            } catch (UnsatisfiedLinkError e) {
                MyTunesRss.QUICKTIME_PLAYER = null;
                if (!MyTunesRss.CONFIG.isQuicktime64BitWarned()) {
                    MyTunesRss.CONFIG.setQuicktime64BitWarned(true);
                    MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.quicktimeUnsatisfiedLink"));
                }
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
        stop();
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
        myCurrentVolume = volume;
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
                setFullScreen(false);
                SwingUtils.invokeAndWait(new Runnable() {
                    public void run() {
                        myMovieFrame.dispose();
                    }
                });
                myCallback.cancelAndCleanup();
                myCallback = null;
                myMovie.stop();
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
        if (myMovie != null) {
            try {
                int width = myMovie.getNaturalBoundsRect().getWidth();
                int height = myMovie.getNaturalBoundsRect().getHeight();
                LOGGER.debug("Video dimension is " + width + " x " + height + ".");
                if (width != 0 & height != 0) {
                    LOGGER.debug("Switching to " + (fullScreen ? "fullscreen" : "window") + " mode.");
                    float rate = myMovie.getRate();
                    myMovie.stop();
                    if (fullScreen) {
                        myMovie.setBounds(myMovie.getNaturalBoundsRect());
                        makeFullscreenMovieFrame(getOptimalDisplayMode(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()));
                        myMovie.setRate(rate);
                        return true;
                    } else {
                        myMovie.setBounds(myMovie.getNaturalBoundsRect());
                        makeWindowMovieFrame();
                        myMovie.setRate(rate);
                    }
                }
            } catch (QTException e) {
                throw new QuicktimePlayerException(e);
            }
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
                boolean fullScreen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getFullScreenWindow() != null;
                stop();
                Track track;
                for (track = myTracks.get(index), myMovie = getMovie(track); myMovie == null && index + 1 < myTracks.size(); index++, track = myTracks.get(index), myMovie = getMovie(track))
                    ;
                if (myMovie == null) {
                    return; // no more tracks to play
                }
                LOGGER.debug("Starting playback of track \"" + track.getName() + "\".");
                myCurrent = index;
                myCallback = new QuicktimePlayerExtremesCallback(myMovie);
                TaskAllMovies.addMovieAndStart();
                setFullScreen(fullScreen);
            } else if (index == -1 && myMovie != null) {
                LOGGER.debug("Continue playback of track \"" + myTracks.get(myCurrent).getName() + "\".");
            }
            setVolume(myCurrentVolume);
            myMovie.start();
            myPlaying = true;
        } catch (QTException
                e) {
            throw new QuicktimePlayerException(e);
        }
    }

    private synchronized DisplayMode getOptimalDisplayMode(GraphicsDevice device) throws StdQTException {
        LOGGER.debug("Getting optimal display mode.");
        if (!device.isDisplayChangeSupported()) {
            LOGGER.debug("No display mode switch supported.");
            // no change supported, return current mode as optimal mode
            return device.getDisplayMode();
        }
        int movieWidth = myMovie.getNaturalBoundsRect().getWidth();
        int movieHeight = myMovie.getNaturalBoundsRect().getHeight();
        LOGGER.debug("Video is " + movieWidth + "x" + movieHeight + " pixels.");
        DisplayMode[] availableModes = device.getDisplayModes();
        List<DisplayMode> bigEnoughModes = new ArrayList<DisplayMode>();
        for (DisplayMode mode : availableModes) {
            if (mode.getWidth() >= movieWidth && mode.getHeight() >= movieHeight) {
                bigEnoughModes.add(mode);
            }
        }
        if (bigEnoughModes.isEmpty()) {
            // no modes are big enough for the video, so return current mode as best mode
            return device.getDisplayMode();
        }
        int dx = Integer.MAX_VALUE;
        int dy = Integer.MAX_VALUE;
        DisplayMode modeMinX = null;
        DisplayMode modeMinY = null;
        for (DisplayMode mode : bigEnoughModes) {
            if (mode.getWidth() - movieWidth < dx || (mode.getWidth() - movieWidth == dx && mode.getBitDepth() > modeMinX.getBitDepth())) {
                dx = mode.getWidth() - movieWidth;
                modeMinX = mode;
            }
            if (mode.getHeight() - movieHeight < dy || (mode.getHeight() - movieHeight == dy && mode.getBitDepth() > modeMinY.getBitDepth())) {
                dy = mode.getHeight() - movieHeight;
                modeMinY = mode;
            }
        }
        long overflowMinX = ((long) modeMinX.getWidth() * (long) modeMinX.getHeight()) - ((long) movieWidth * (long) movieHeight);
        long overflowMinY = ((long) modeMinY.getWidth() * (long) modeMinY.getHeight()) - ((long) movieWidth * (long) movieHeight);
        return overflowMinX < overflowMinY ? modeMinX : modeMinY;
    }

    private synchronized void makeFullscreenMovieFrame(DisplayMode displayMode) throws QTException {
        LOGGER.debug("Making fullscreen movie frame.");
        int movieWidth = myMovie.getNaturalBoundsRect().getWidth();
        int movieHeight = myMovie.getNaturalBoundsRect().getHeight();
        float xFactor = (float) displayMode.getWidth() / (float) movieWidth;
        float yFactor = (float) displayMode.getHeight() / (float) movieHeight;
        if (xFactor > yFactor) {
            movieWidth *= yFactor;
            movieHeight *= yFactor;
        } else {
            movieWidth *= xFactor;
            movieHeight *= xFactor;
        }
        Dimension topBottomDim = new Dimension(displayMode.getWidth(), (displayMode.getHeight() - movieHeight) / 2);
        Dimension leftRightDim = new Dimension((displayMode.getWidth() - movieWidth) / 2, displayMode.getHeight());
        final JPanel northPanel = new JPanel();
        northPanel.setPreferredSize(topBottomDim);
        northPanel.setBackground(Color.BLACK);
        final JPanel southPanel = new JPanel();
        southPanel.setPreferredSize(topBottomDim);
        southPanel.setBackground(Color.BLACK);
        final JPanel westPanel = new JPanel();
        westPanel.setPreferredSize(leftRightDim);
        westPanel.setBackground(Color.BLACK);
        final JPanel eastPanel = new JPanel();
        eastPanel.setPreferredSize(leftRightDim);
        eastPanel.setBackground(Color.BLACK);
        final Component videoComponent = QTFactory.makeQTComponent(myMovie).asComponent();
        SwingUtils.invokeAndWait(new Runnable() {
            public void run() {
                myMovieFrame.dispose();
                myMovieFrame.setUndecorated(true);
                myMovieFrame.setResizable(false);
                myMovieFrame.getContentPane().removeAll();
                myMovieFrame.getContentPane().add(new JLabel());
                myMovieFrame.pack();
                myMovieFrame.setVisible(true);
                myMovieFrame.setVisible(false);
                myMovieFrame.getContentPane().removeAll();
                myMovieFrame.getContentPane().add(BorderLayout.NORTH, northPanel);
                myMovieFrame.getContentPane().add(BorderLayout.SOUTH, southPanel);
                myMovieFrame.getContentPane().add(BorderLayout.WEST, westPanel);
                myMovieFrame.getContentPane().add(BorderLayout.EAST, eastPanel);
                myMovieFrame.getContentPane().add(BorderLayout.CENTER, videoComponent);
                myMovieFrame.pack();
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(myMovieFrame);
                myMovieFrame.setVisible(true);
            }
        });
    }

    private synchronized void makeWindowMovieFrame() throws QTException {
        LOGGER.debug("Making window movie frame.");
        final Component videoComponent = QTFactory.makeQTComponent(myMovie).asComponent();
        SwingUtils.invokeAndWait(new Runnable() {
            public void run() {
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
                myMovieFrame.dispose();
                myMovieFrame.setTitle("MyTunesRSS Video");
                myMovieFrame.getContentPane().removeAll();
                myMovieFrame.getContentPane().add(new JLabel(""));
                myMovieFrame.pack();
                myMovieFrame.setVisible(true);
                myMovieFrame.setVisible(false);
                myMovieFrame.getContentPane().removeAll();
                myMovieFrame.getContentPane().add(videoComponent);
                myMovieFrame.pack();
                myMovieFrame.setVisible(true);
            }
        });
    }

    /**
     * Get a new movie instance from either the track's file or (for a you tube remote file) from the external
     * url.
     *
     * @param track The track.
     * @return The corresponding movie.
     * @throws QTException Any exception.
     */
    private synchronized Movie getMovie(Track track) {
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

    private synchronized void onFinishPlayback() {
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

    class QuicktimePlayerExtremesCallback extends ExtremesCallBack {
        QuicktimePlayerExtremesCallback(Movie movie) throws QTException {
            super(movie.getTimeBase(), StdQTConstants.triggerAtStop);
            callMeWhen();
        }

        public void execute() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    onFinishPlayback();
                }
            });
        }
    }
}