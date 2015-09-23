package de.codewave.camel.mp3;

import java.util.HashMap;
import java.util.Map;

public class Mp3Header {

    public static enum Version {
        MPEG1, MPEG2, MPEG25, RESERVED;
    }

    public static enum Layer {
        LAYER_I, LAYER_II, LAYER_III, RESERVED;
    }

    public static enum ChannelMode {
        STEREO, JOINT_STEREO, DUAL, SINGLE;
    }

    public static enum Emphasis {
        NONE, MS_50_15, CCIT_J_17, RESERVED;
    }

    public static boolean isValidSync(long sync) {
        if ((sync & 0xFFE00000L) != 0xFFE00000L) {
            return false; // illegal sync
        }
        if ((sync & 0x00180000L) == 0x00080000L) {
            return false; // illegal version
        }
        if ((sync & 0x00060000L) == 0) {
            return false; // illegal version
        }
        if ((sync & 0x0000F000L) == 0x0000F000L) {
            return false; // illegal bitrate
        }
        if ((sync & 0x0000F000L) == 0x00000000L) {
            return false; // illegal bitrate
        }
        if ((sync & 0x00000C00L) == 0x00000C00L) {
            return false; // illegal sample rate
        }
        if ((sync & 0x00000003L) == 0x00000002L) {
            return false; // illegal emphasis
        }
        return true;
    }

    private static final Map<Long, Version> VERSION_MAPPING = new HashMap<Long, Version>();
    private static final Map<Long, Layer> LAYER_MAPPING = new HashMap<Long, Layer>();
    private static final Map<Long, ChannelMode> CHANNEL_MODE_MAPPING = new HashMap<Long, ChannelMode>();
    private static final Map<Long, Emphasis> EMPHASIS_MAPPING = new HashMap<Long, Emphasis>();
    private static final Map<Long, long[]> BITRATE_MAPPING = new HashMap<Long, long[]>();
    private static final Map<Version, long[]> SAMPLERATE_MAPPING = new HashMap<Version, long[]>();

    static {
        VERSION_MAPPING.put(0L, Version.MPEG25);
        VERSION_MAPPING.put(1L, Version.RESERVED);
        VERSION_MAPPING.put(2L, Version.MPEG2);
        VERSION_MAPPING.put(3L, Version.MPEG1);
        LAYER_MAPPING.put(0L, Layer.RESERVED);
        LAYER_MAPPING.put(1L, Layer.LAYER_III);
        LAYER_MAPPING.put(2L, Layer.LAYER_II);
        LAYER_MAPPING.put(3L, Layer.LAYER_I);
        CHANNEL_MODE_MAPPING.put(0L, ChannelMode.STEREO);
        CHANNEL_MODE_MAPPING.put(1L, ChannelMode.JOINT_STEREO);
        CHANNEL_MODE_MAPPING.put(2L, ChannelMode.DUAL);
        CHANNEL_MODE_MAPPING.put(3L, ChannelMode.SINGLE);
        EMPHASIS_MAPPING.put(0L, Emphasis.NONE);
        EMPHASIS_MAPPING.put(1L, Emphasis.MS_50_15);
        EMPHASIS_MAPPING.put(2L, Emphasis.RESERVED);
        EMPHASIS_MAPPING.put(3L, Emphasis.CCIT_J_17);
        BITRATE_MAPPING.put(0L, new long[]{0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, 0});
        BITRATE_MAPPING.put(1L, new long[]{0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, 0});
        BITRATE_MAPPING.put(2L, new long[]{0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 0});
        BITRATE_MAPPING.put(3L, new long[]{0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, 0});
        BITRATE_MAPPING.put(4L, new long[]{0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, 0});
        SAMPLERATE_MAPPING.put(Version.MPEG1, new long[]{44100, 48000, 32000, 0});
        SAMPLERATE_MAPPING.put(Version.MPEG2, new long[]{22050, 24000, 16000, 0});
        SAMPLERATE_MAPPING.put(Version.MPEG25, new long[]{11025, 12000, 8000, 0});
    }

    private boolean myProtected;
    private long mySampleRate;
    private long myBitRate;
    private Version myVersion;
    private Layer myLayer;
    private ChannelMode myChannelMode;
    private boolean myPadded;
    private boolean myCopyright;
    private boolean myOriginal;
    private Emphasis myEmphasis;
    private long myModeExtension;

    public Mp3Header(long syncValue) {
        if (!isValidSync(syncValue)) {
            throw new IllegalArgumentException("Invalid MP3 audio frame sync value \"" + syncValue + "\".");
        }
        myVersion = VERSION_MAPPING.get((syncValue >> 19) & 0x03L);
        myLayer = LAYER_MAPPING.get((syncValue >> 17) & 0x03L);
        myProtected = ((syncValue >> 16) & 0x01L) == 0x00L;
        int bitrate = (int) ((syncValue >> 12) & 0x0FL);
        if (myVersion == Version.MPEG1 && myLayer == Layer.LAYER_I) {
            myBitRate = BITRATE_MAPPING.get(0L)[bitrate];
        } else if (myVersion == Version.MPEG1 && myLayer == Layer.LAYER_II) {
            myBitRate = BITRATE_MAPPING.get(1L)[bitrate];
        } else if (myVersion == Version.MPEG1 && myLayer == Layer.LAYER_III) {
            myBitRate = BITRATE_MAPPING.get(2L)[bitrate];
        } else if (myVersion == Version.MPEG2 && myLayer == Layer.LAYER_I) {
            myBitRate = BITRATE_MAPPING.get(3L)[bitrate];
        } else if (myVersion == Version.MPEG2 && (myLayer == Layer.LAYER_II || myLayer == Layer.LAYER_III)) {
            myBitRate = BITRATE_MAPPING.get(4L)[bitrate];
        }
        myBitRate *= 1000;
        mySampleRate = SAMPLERATE_MAPPING.get(myVersion)[((int) ((syncValue >> 10) & 0x03L))];
        if (bitrate == 0 || mySampleRate == 0) {
            throw new IllegalArgumentException("Invalid MP3 audio frame sync value \"" + syncValue + "\".");
        }
        myPadded = ((syncValue >> 9) & 0x01L) == 0x01L;
        myChannelMode = CHANNEL_MODE_MAPPING.get((syncValue >> 6) & 0x03L);
        myModeExtension = (syncValue >> 4) & 0x03L;
        myCopyright = ((syncValue >> 3) & 0x01L) == 0x01L;
        myOriginal = ((syncValue >> 2) & 0x01L) == 0x01L;
        myEmphasis = EMPHASIS_MAPPING.get(syncValue & 0x02L);
    }

    public boolean isProtected() {
        return myProtected;
    }

    public long getSampleRate() {
        return mySampleRate;
    }

    public long getBitRate() {
        return myBitRate;
    }

    public Version getVersion() {
        return myVersion;
    }

    public Layer getLayer() {
        return myLayer;
    }

    public ChannelMode getChannelMode() {
        return myChannelMode;
    }

    public boolean isPadded() {
        return myPadded;
    }

    public boolean isCopyright() {
        return myCopyright;
    }

    public boolean isOriginal() {
        return myOriginal;
    }

    public Emphasis getEmphasis() {
        return myEmphasis;
    }

    public long getModeExtension() {
        return myModeExtension;
    }

    public long getFrameLength() {
        if (myLayer == Layer.LAYER_I) {
            return  ((12 * myBitRate / mySampleRate) + (myPadded ? 1 : 0)) * 4;
        } else if (myLayer == Layer.LAYER_II) {
            return (144 * myBitRate / mySampleRate) + (myPadded ? 1 : 0);
        } else {
            if (myVersion == Version.MPEG1) {
                return (144 * myBitRate / mySampleRate) + (myPadded ? 1 : 0);
            } else {
                return (72 * myBitRate / mySampleRate) + (myPadded ? 1 : 0);
            }
        }
    }

    public double getFrameDurationMillis() {
        if (myLayer == Layer.LAYER_I) {
            return 384000d / mySampleRate;
        } else if (myLayer == Layer.LAYER_II) {
            return 1152000d / mySampleRate;
        } else {
            if (myVersion == Version.MPEG1) {
                return 1152000d / mySampleRate;
            } else {
                return 576000d / mySampleRate;
            }
        }
    }

    @Override
    public String toString() {
        return "Mp3Header{" +
                "myProtected=" + myProtected +
                ", mySampleRate=" + mySampleRate +
                ", myBitRate=" + myBitRate +
                ", myVersion=" + myVersion +
                ", myLayer=" + myLayer +
                ", myChannelMode=" + myChannelMode +
                ", myPadded=" + myPadded +
                ", myCopyright=" + myCopyright +
                ", myOriginal=" + myOriginal +
                ", myEmphasis=" + myEmphasis +
                ", myModeExtension=" + myModeExtension +
                '}';
    }
}
