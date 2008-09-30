package de.codewave.mytunesrss.remote;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.remote.JsonpFilterOutputStream
 */
public class JsonpFilterOutputStream extends ServletOutputStream {
    private boolean myPreludeWritten;
    private String myEncoding;
    private ServletOutputStream myDelegate;
    private String myPrelude;
    private String myCoda;

    public JsonpFilterOutputStream(ServletOutputStream delegate, String encoding, String prelude, String coda) {
        myDelegate = delegate;
        myEncoding = encoding;
        myPrelude = prelude;
        myCoda = coda;
    }

    public void write(int b) throws IOException {
        if (!myPreludeWritten) {
            myPreludeWritten = true;
            myDelegate.write(myPrelude.getBytes(myEncoding));
        }
        myDelegate.write(b);
    }

    @Override
    public void flush() throws IOException {
        myDelegate.flush();
    }

    @Override
    public void close() throws IOException {
        myDelegate.write(myCoda.getBytes(myEncoding));
        myDelegate.close();
    }
}