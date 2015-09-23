package de.codewave.camel.mp3;

/**
 * Represents information collected from the MP3 audio frame headers of an MP3 file.
 */
public class Mp3Info {
    private long myMinBitrate;
    private long myMaxBitrate;
    private long myAvgBitrate;
    private long myMinSampleRate;
    private long myMaxSampleRate;
    private long myAvgSampleRate;
    private int myDurationSeconds;

    public Mp3Info(long minBitrate, long maxBitrate, long avgBitrate, long minSampleRate, long maxSampleRate, long avgSampleRate, int durationSeconds) {
        myMinBitrate = minBitrate;
        myMaxBitrate = maxBitrate;
        myAvgBitrate = avgBitrate;
        myMinSampleRate = minSampleRate;
        myMaxSampleRate = maxSampleRate;
        myAvgSampleRate = avgSampleRate;
        myDurationSeconds = durationSeconds;
    }

    public boolean isVbr() {
        return myMaxBitrate != myMinBitrate;
    }

    public long getMinBitrate() {
        return myMinBitrate;
    }

    public long getMaxBitrate() {
        return myMaxBitrate;
    }

    public long getAvgBitrate() {
        return myAvgBitrate;
    }

    public long getMinSampleRate() {
        return myMinSampleRate;
    }

    public long getMaxSampleRate() {
        return myMaxSampleRate;
    }

    public long getAvgSampleRate() {
        return myAvgSampleRate;
    }

    public int getDurationSeconds() {
        return myDurationSeconds;
    }

    @Override
    public String toString() {
        return "Mp3Info{" +
                "myMinBitrate=" + myMinBitrate +
                ", myMaxBitrate=" + myMaxBitrate +
                ", myAvgBitrate=" + myAvgBitrate +
                ", myMinSampleRate=" + myMinSampleRate +
                ", myMaxSampleRate=" + myMaxSampleRate +
                ", myAvgSampleRate=" + myAvgSampleRate +
                ", myDurationSeconds=" + myDurationSeconds +
                '}';
    }
}
