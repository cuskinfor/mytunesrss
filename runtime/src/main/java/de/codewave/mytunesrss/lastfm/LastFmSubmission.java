package de.codewave.mytunesrss.lastfm;

import de.codewave.mytunesrss.datastore.statement.Track;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * de.codewave.mytunesrss.lastfm.LastFmSubmission
 */
public class LastFmSubmission implements Delayed {
    private Track myTrack;
    private long myPlaybackStartTime;
    private boolean myFinished;

    public LastFmSubmission(Track track, long playbackStartTime) {
        myTrack = track;
        myPlaybackStartTime = playbackStartTime;
    }

    public void setFinished(boolean finished) {
        myFinished = finished;
    }

    public long getPlaybackStartTime() {
        return myPlaybackStartTime;
    }

    public void setPlaybackStartTime(long playbackStartTime) {
        myPlaybackStartTime = playbackStartTime;
    }

    public Track getTrack() {
        return myTrack;
    }

    public void setTrack(Track track) {
        myTrack = track;
    }

    public long getDelay(TimeUnit unit) {
        if (!myFinished) {
            long delay = myPlaybackStartTime + (1000L * myTrack.getTime()) - System.currentTimeMillis();
            if (delay > 0) {
                return unit.convert(delay, TimeUnit.MILLISECONDS);
            }
        }
        return 0;
    }

    public int compareTo(Delayed o) {
        return (int) (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LastFmSubmission)) return false;

        LastFmSubmission that = (LastFmSubmission) o;

        if (getDelay(TimeUnit.MILLISECONDS) != that.getDelay(TimeUnit.MILLISECONDS)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        long delay = getDelay(TimeUnit.MILLISECONDS);
        return (int) (delay ^ (delay >>> 32));
    }
}
