package de.codewave.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.OutputStream;

/**
 * de.codewave.utils.Base64OutputStream
 */
public class Base64OutputStream extends OutputStream {
    public static final int FLUSH_SIZE = 4096;

    private OutputStream myOutputStream;
    private byte[] myBuffer = new byte[FLUSH_SIZE];
    private int myIndex = 0;
    private String myEncoding = "UTF-8";
    private int myLineLength;
    private int myCurrentLineLength;

    public Base64OutputStream(OutputStream outputStream) {
        myOutputStream = outputStream;
    }

    public void setLineLength(int lineLength) {
        myLineLength = lineLength;
    }

    public void setEncoding(String encoding) {
        myEncoding = encoding;
    }

    public synchronized void write(int i) throws IOException {
        myBuffer[myIndex++] = (byte)i;
        if (myIndex == FLUSH_SIZE) {
            flush();
        }
    }

    public synchronized void flush() throws IOException {
        int blocks = myIndex / 3;
        if (blocks > 0) {
            byte[] blockData = new byte[blocks * 3];
            copyAndWriteBytes(blockData);
        }
    }

    private void copyAndWriteBytes(byte[] blockData) throws IOException {
        System.arraycopy(myBuffer, 0, blockData, 0, blockData.length);
        String encoded = new String(Base64.encodeBase64(blockData), "UTF-8");
        if (myLineLength > 0) {
            while (myCurrentLineLength + encoded.length() > myLineLength) {
                myOutputStream.write(encoded.substring(0, myLineLength - myCurrentLineLength).getBytes(myEncoding));
                myOutputStream.write(System.getProperty("line.separator").getBytes(myEncoding));
                encoded = encoded.substring(myLineLength - myCurrentLineLength);
                myCurrentLineLength = 0;
            }
        }
        myOutputStream.write(encoded.getBytes(myEncoding));
        myCurrentLineLength += encoded.length();
        if (blockData.length < myIndex) {
            System.arraycopy(myBuffer, blockData.length, myBuffer, 0, myIndex - blockData.length);
        }
        myIndex -= blockData.length;
    }

    public synchronized void close() throws IOException {
        flush();
        if (myIndex > 0) {
            byte[] blockData = new byte[myIndex];
            copyAndWriteBytes(blockData);
        }
    }
}