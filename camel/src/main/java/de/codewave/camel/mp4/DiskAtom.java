package de.codewave.camel.mp4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;

import java.util.List;

public class DiskAtom extends Mp4Atom {
    DiskAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    public int getNumber() {
        return CamelUtils.getIntValue(getFirstChild("data").getData(), getFirstChild("data").getBodySize() - 4, 2, false, Endianness.Big);
    }

    public int getSize() {
        return CamelUtils.getIntValue(getFirstChild("data").getData(), getFirstChild("data").getBodySize() - 2, 2, false, Endianness.Big);
    }
}
