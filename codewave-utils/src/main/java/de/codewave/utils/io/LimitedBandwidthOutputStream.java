package de.codewave.utils.io;

import java.io.*;

public class LimitedBandwidthOutputStream extends OutputStream {
    private OutputStream myStream;

    private int myNanosPerKbyte;

    private int myCounter;

    private long myStartNanos;

    private int myFastWriteLength;

    /**
     * Create a bandwidth limited output stream. The stream wraps another stream which is limited by the specified bitrate in kbit/s.
     *
     * @param stream    Output stream to wrap.
     * @param bandwidth Bandwidth limit in kbit/s
     */
    public LimitedBandwidthOutputStream(OutputStream stream, int bandwidth) {
        myStream = stream;
        double kbytePerSecond = (double)bandwidth / 8d;
        myNanosPerKbyte = (int)(1000000d * (1000d / kbytePerSecond));
    }

    @Override
    public void write(int b) throws IOException {
        if (myStartNanos == 0) {
            // lazy init of start time
            myStartNanos = System.nanoTime();
        }
        myStream.write(b);
        myCounter++;
        if (myCounter == 1024) {
            // 1 kb written
            for (long currentNanos = System.nanoTime(); currentNanos - myStartNanos < myNanosPerKbyte; currentNanos = System
                    .nanoTime()) {
                Thread.yield();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // ignore exception and keep waiting in while-loop
                }
            }
            myCounter = 0;
            myStartNanos = System.nanoTime();
        }
    }
}
