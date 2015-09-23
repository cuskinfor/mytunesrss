package de.codewave.camel.mp4;

import java.util.List;

public class CompilationAtom extends Mp4Atom {
    CompilationAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

    public boolean isCompilation() {
        return getFirstChild("data").getData()[getFirstChild("data").getBodySize() - 1] > 0;
    }
}
