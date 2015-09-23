package de.codewave.camel.mp4;

import java.util.List;

public class SampleDescriptionsAtom extends Mp4Atom {

    SampleDescriptionsAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
    }

}
