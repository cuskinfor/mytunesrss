package de.codewave.camel.mp4;

import de.codewave.camel.mp4.Mp4Atom;

import java.util.List;

public class StikAtom extends Mp4Atom {

    public static enum Type {
        Movie(), Normal(), Audiobook(), MusicVideo(), TvShow(), Booklet(), Ringtone();
    }

    private Type myType = Type.Normal;

    StikAtom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        super(path, offset, children, data, atomSize);
        if (!getChildren().isEmpty()) {
            Mp4Atom dataChildAtom = getChildren().get(0);
            if (dataChildAtom != null && dataChildAtom.getData() != null && dataChildAtom.getBodySize() > 0) {
                switch (dataChildAtom.getData()[dataChildAtom.getBodySize() - 1]) {
                    case 0:
                    case 9:
                        myType = Type.Movie;
                        break;
                    case 2:
                        myType = Type.Audiobook;
                        break;
                    case 6:
                        myType = Type.MusicVideo;
                        break;
                    case 10:
                        myType = Type.TvShow;
                        break;
                    case 11:
                        myType = Type.Booklet;
                        break;
                    case 14:
                        myType = Type.Ringtone;
                        break;
                    default:
                        myType = Type.Normal;
                }
            }
        }
    }

    public Type getType() {
        return myType;
    }
}
