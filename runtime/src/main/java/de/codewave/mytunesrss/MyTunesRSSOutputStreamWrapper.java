package de.codewave.mytunesrss;

import de.codewave.utils.io.LimitedBandwidthOutputStream;
import de.codewave.utils.servlet.StreamSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * de.codewave.mytunesrss.MyTunesRSSOutputStreamWrapper
 */
public class MyTunesRSSOutputStreamWrapper implements StreamSender.OutputStreamWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRSSOutputStreamWrapper.class);
    private int myBitrate;
    private int myDataOffset;
    private int myFirstByte;
    private int myPreStreamSeconds;

    public MyTunesRSSOutputStreamWrapper(int bitrate, int dataOffset, int firstByte, int preStreamSeconds) {
        myBitrate = bitrate;
        myDataOffset = dataOffset;
        myFirstByte = firstByte;
        myPreStreamSeconds = preStreamSeconds;
    }

    public OutputStream wrapStream(final OutputStream outputStream) {
        final LimitedBandwidthOutputStream limitedStream = new LimitedBandwidthOutputStream(outputStream, myBitrate);
        int preStreamAtFullSpeed = myPreStreamSeconds * 125 * myBitrate;
        if (myFirstByte < myDataOffset) {
            preStreamAtFullSpeed += (myDataOffset - myFirstByte);// mp3 header
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Transferring " + preStreamAtFullSpeed + " bytes without limit.");
        }
        return new PrefillBufferLimitedOutputStream(outputStream, limitedStream, preStreamAtFullSpeed);
    }

    public class PrefillBufferLimitedOutputStream extends OutputStream {
        int mySpeedBytesRemain;
        OutputStream mySpeedStream;
        OutputStream myLimitedStream;

        public PrefillBufferLimitedOutputStream(OutputStream speedStream, OutputStream limitedStream, int speedBytesRemain) {
            mySpeedStream = speedStream;
            myLimitedStream = limitedStream;
            mySpeedBytesRemain = speedBytesRemain;
        }

        public void write(int b) throws IOException {
            if (mySpeedBytesRemain > 0) {
                mySpeedStream.write(b);
                mySpeedBytesRemain--;
            } else {
                myLimitedStream.write(b);
            }
        }
    }
}