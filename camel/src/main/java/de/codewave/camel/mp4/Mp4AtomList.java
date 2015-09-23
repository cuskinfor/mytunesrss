package de.codewave.camel.mp4;

import java.util.ArrayList;
import java.util.List;

public class Mp4AtomList {

    private Mp4Atom myRoot;

    Mp4AtomList(Mp4AtomFactory atomFactory, List<Mp4Atom> atoms) {
        myRoot = atomFactory.create("dummy", 0, atoms, null, 0);
    }

    public Mp4Atom getFirst(String name) {
        return myRoot.getFirstChild(name);
    }

    public List<Mp4Atom> get(String name) {
        return myRoot.getChildren(name);
    }

    public List<Mp4Atom> toList() {
        return new ArrayList<Mp4Atom>(myRoot.getChildren());
    }
}
