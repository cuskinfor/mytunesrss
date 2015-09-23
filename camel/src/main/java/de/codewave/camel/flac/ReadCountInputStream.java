package de.codewave.camel.flac;

import java.io.IOException;
import java.io.InputStream;

/**
 * de.codewave.camel.flac.ReadCountInputStream
 */
public class ReadCountInputStream extends InputStream {
    private InputStream myInputStream;
    private long count = 0;

    public ReadCountInputStream(InputStream inputStream) {
        myInputStream = inputStream;
    }

    public int read() throws IOException {
        int value = myInputStream.read();
        if (value != -1) {
            count++;
        }
        return value;
    }

    @Override
    public void close() throws IOException {
        myInputStream.close();
    }

    public long getCount() {
        return count;
    }
}