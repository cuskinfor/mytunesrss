package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.meta.Image;

import java.io.IOException;
import java.io.InputStream;

public class DatabaseImage extends Image {
    
    private final long myLastUpdate;
    
    public DatabaseImage(String mimeType, InputStream is, long lastUpdate) throws IOException {
        super(mimeType, is);
        myLastUpdate = lastUpdate;
    }

    public long getLastUpdate() {
        return myLastUpdate;
    }
}
