package de.codewave.camel.mp4;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class CoverAtom extends Mp4Atom {
    CoverAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    public String getMimeType() {
        return getFirstChild("data").getData()[3] == 13 ? "image/jpeg" : "image/png";
    }

    public InputStream getDataStream() {
        return new ByteArrayInputStream(getFirstChild("data").getData(), 8, getFirstChild("data").getBodySize() - 8);
    }
}
