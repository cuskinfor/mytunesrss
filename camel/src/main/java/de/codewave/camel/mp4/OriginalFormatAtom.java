package de.codewave.camel.mp4;

import java.util.List;

public class OriginalFormatAtom extends Mp4Atom {
    public OriginalFormatAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    public String getValue() {
        return getDataAsString(0, 4, "UTF-8");
    }
}
