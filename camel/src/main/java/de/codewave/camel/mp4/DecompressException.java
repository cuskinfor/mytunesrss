package de.codewave.camel.mp4;

public class DecompressException extends Exception {
    public DecompressException(String s) {
        super(s);
    }

    public DecompressException(String s, Throwable t) {
        super(s, t);
    }
}
