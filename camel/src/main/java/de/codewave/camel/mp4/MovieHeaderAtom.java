package de.codewave.camel.mp4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;

import java.util.List;

public class MovieHeaderAtom extends Mp4Atom {
    public MovieHeaderAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }
    
    public int getVersion() {
        return getData()[0];
    }
    
    public int getFlags() {
        return CamelUtils.getIntValue(getData(), 1, 3, false, Endianness.Big);
    }
    
    public long getCreationTime() {
        return CamelUtils.getLongValue(getData(), 4, 4, false, Endianness.Big);
    }
    
    public long getModificationTime() {
        return CamelUtils.getLongValue(getData(), 8, 4, false, Endianness.Big);
    }
    
    public long getTimeScale() {
        return CamelUtils.getLongValue(getData(), 12, 4, false, Endianness.Big);
    }
    
    public long getDuration() {
        return CamelUtils.getLongValue(getData(), 16, 4, false, Endianness.Big);
    }

    public long getPreferredRate() {
        return CamelUtils.getLongValue(getData(), 20, 4, false, Endianness.Big);
    }

    public long getPreferredVolume() {
        return CamelUtils.getLongValue(getData(), 24, 2, false, Endianness.Big);
    }

    public long getPreviewTime() {
        return CamelUtils.getLongValue(getData(), 72, 4, false, Endianness.Big);
    }

    public long getPreviewDuration() {
        return CamelUtils.getLongValue(getData(), 76, 4, false, Endianness.Big);
    }

    public long getPosterTime() {
        return CamelUtils.getLongValue(getData(), 80, 4, false, Endianness.Big);
    }

    public long getSelectionTime() {
        return CamelUtils.getLongValue(getData(), 84, 4, false, Endianness.Big);
    }

    public long getSelectionDuration() {
        return CamelUtils.getLongValue(getData(), 88, 4, false, Endianness.Big);
    }

    public long getCurrentTime() {
        return CamelUtils.getLongValue(getData(), 92, 4, false, Endianness.Big);
    }

    public long getNextTrackID() {
        return CamelUtils.getLongValue(getData(), 96, 4, false, Endianness.Big);
    }
}
