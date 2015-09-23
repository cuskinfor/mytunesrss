package de.codewave.camel.mp4;

import java.util.List;

public class GenreAtom extends Mp4Atom {
    GenreAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    public String getGenre() {
        return getFirstChild("data").getDataAsString(8, "UTF-8");
    }
}
