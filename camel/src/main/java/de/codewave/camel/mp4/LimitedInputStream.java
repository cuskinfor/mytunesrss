package de.codewave.camel.mp4;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {

    private InputStream myDelegate;
    private long myRemainingSize;
    private boolean myCloseDelegate;

    public LimitedInputStream(InputStream delegate, long remainingSize) {
        myDelegate = delegate;
        myRemainingSize = remainingSize;
    }

    public void setCloseDelegate(boolean closeDelegate) {
        myCloseDelegate = closeDelegate;
    }

    @Override
    public int read() throws IOException {
        if (myRemainingSize == 0) {
            return -1;
        }
        int i = myDelegate.read();
        if (i > -1) {
            myRemainingSize--;
        }
        return i;
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (myRemainingSize == 0) {
            return -1;
        }
        int i = myDelegate.read(b, 0, (int)Math.min(b.length, myRemainingSize));
        if (i > -1) {
            myRemainingSize -= i;
        }
        return i;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (myRemainingSize == 0) {
            return -1;
        }
        int i = myDelegate.read(b, off, (int)Math.min(len, myRemainingSize));
        if (i > -1) {
            myRemainingSize -= i;
        }
        return i;
    }

    @Override
    public long skip(long n) throws IOException {
        if (myRemainingSize == 0) {
            return 0;
        }
        long skipped = myDelegate.skip(Math.min(n, myRemainingSize));
        myRemainingSize -= skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        if (myRemainingSize == 0) {
            return 0;
        }
        return Math.min(myDelegate.available(), (int)Math.min(Integer.MAX_VALUE, myRemainingSize));
    }

    @Override
    public void close() throws IOException {
        if (myCloseDelegate) {
            myDelegate.close();
        }
    }

    @Override
    public void mark(int readlimit) {
        throw new UnsupportedOperationException("Mark and reset not supported!");
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException("Mark and reset not supported!");
    }

    @Override
    public boolean markSupported() {
        return false;
    }
}
