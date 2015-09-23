package de.codewave.camel.flac;

import de.codewave.camel.Endianness;

import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.camel.flac.FlacMetadataStreaminfo
 */
public class FlacMetadataStreaminfo implements FlacMetadataData {
    /*
    <16>   	 The minimum block size (in samples) used in the stream.
<16> 	The maximum block size (in samples) used in the stream. (Minimum blocksize == maximum blocksize) implies a fixed-blocksize stream.
<24> 	The minimum frame size (in bytes) used in the stream. May be 0 to imply the value is not known.
<24> 	The maximum frame size (in bytes) used in the stream. May be 0 to imply the value is not known.
<20> 	Sample rate in Hz. Though 20 bits are available, the maximum sample rate is limited by the structure of frame headers to 655350Hz. Also, a value of 0 is invalid.
<3> 	(number of channels)-1. FLAC supports from 1 to 8 channels
<5> 	(bits per sample)-1. FLAC supports from 4 to 32 bits per sample. Currently the reference encoder and decoders only support up to 24 bits per sample.
<36> 	Total samples in stream. 'Samples' means inter-channel sample, i.e. one second of 44.1Khz audio will have 44100 samples regardless of the number of channels. A value of zero here means the number of total samples is unknown.
<128> 	MD5 signature of the unencoded audio data. This allows the decoder to determine if an error exists in the audio data even when the error does not result in an invalid bitstream.
     */
    private int myMinimumBlockSize;
    private int myMaximumBlockSize;
    private int myMinimumFrameSize;
    private int myMaximumFrameSize;
    private int mySampleRate;
    private int myChannelCount;
    private int myBitsPerSample;
    private long myTotalSamplesCount;
    private byte[] myMd5Signature;

    public FlacMetadataStreaminfo(InputStream is) throws IOException {
        myMinimumBlockSize = (int) FlacUtils.getValue(is, 2, Endianness.Big);
        myMaximumBlockSize = (int) FlacUtils.getValue(is, 2, Endianness.Big);
        myMinimumFrameSize = (int) FlacUtils.getValue(is, 3, Endianness.Big);
        myMaximumFrameSize = (int) FlacUtils.getValue(is, 3, Endianness.Big);
        byte[] buffer = new byte[8];
        if (is.read(buffer) != 8) {
            throw new IOException("Could not read enough data from stream.");
        }
        mySampleRate = (int) FlacUtils.getValue(FlacUtils.getBits(buffer, 0, 20), Endianness.Big);
        myChannelCount = (int) FlacUtils.getValue(FlacUtils.getBits(buffer, 20, 3), Endianness.Big);
        myBitsPerSample = (int) FlacUtils.getValue(FlacUtils.getBits(buffer, 23, 5), Endianness.Big);
        myTotalSamplesCount = FlacUtils.getValue(FlacUtils.getBits(buffer, 28, 36), Endianness.Big);
        myMd5Signature = new byte[16];
        if (is.read(myMd5Signature) != 16) {
            throw new IOException("Could not read enough data from stream.");
        }
    }

    public int getMinimumBlockSize() {
        return myMinimumBlockSize;
    }

    public void setMinimumBlockSize(int minimumBlockSize) {
        myMinimumBlockSize = minimumBlockSize;
    }

    public int getMaximumBlockSize() {
        return myMaximumBlockSize;
    }

    public void setMaximumBlockSize(int maximumBlockSize) {
        myMaximumBlockSize = maximumBlockSize;
    }

    public int getMinimumFrameSize() {
        return myMinimumFrameSize;
    }

    public void setMinimumFrameSize(int minimumFrameSize) {
        myMinimumFrameSize = minimumFrameSize;
    }

    public int getMaximumFrameSize() {
        return myMaximumFrameSize;
    }

    public void setMaximumFrameSize(int maximumFrameSize) {
        myMaximumFrameSize = maximumFrameSize;
    }

    public int getSampleRate() {
        return mySampleRate;
    }

    public void setSampleRate(int sampleRate) {
        mySampleRate = sampleRate;
    }

    public int getChannelCount() {
        return myChannelCount;
    }

    public void setChannelCount(int channelCount) {
        myChannelCount = channelCount;
    }

    public int getBitsPerSample() {
        return myBitsPerSample;
    }

    public void setBitsPerSample(int bitsPerSample) {
        myBitsPerSample = bitsPerSample;
    }

    public long getTotalSamplesCount() {
        return myTotalSamplesCount;
    }

    public void setTotalSamplesCount(long totalSamplesCount) {
        myTotalSamplesCount = totalSamplesCount;
    }

    public byte[] getMd5Signature() {
        return myMd5Signature;
    }

    public void setMd5Signature(byte[] md5Signature) {
        myMd5Signature = md5Signature;
    }
}