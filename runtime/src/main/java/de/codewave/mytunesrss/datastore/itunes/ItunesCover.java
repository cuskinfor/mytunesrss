package de.codewave.mytunesrss.datastore.itunes;

import java.io.File;

/**
 * de.codewave.mytunesrss.datastore.itunes.ItunesCover
 */
public class ItunesCover {
    private File myFile;
    private String myMimeType;
    private int myOffset;

    public ItunesCover(File file, String mimeType, int offset) {
        myFile = file;
        myMimeType = mimeType;
        myOffset = offset;
    }

    public File getFile() {
        return myFile;
    }

    public String getMimeType() {
        return myMimeType;
    }

    public int getOffset() {
        return myOffset;
    }
}