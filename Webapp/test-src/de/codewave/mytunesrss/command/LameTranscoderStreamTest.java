package de.codewave.mytunesrss.command;

import junit.framework.*;

/**
 * de.codewave.mytunesrss.command.LameTranscoderStreamTest
 */
public class LameTranscoderStreamTest extends TestCase {
    public static Test suite() {
        return new TestSuite(LameTranscoderStreamTest.class);
    }

    public LameTranscoderStreamTest(String string) {
        super(string);
    }

    public void testLameSampleRate() {
        assertEquals("11.025", LameTranscoderStream.getLameSampleRate(11025));
        assertEquals("22.05", LameTranscoderStream.getLameSampleRate(22050));
        assertEquals("44.1", LameTranscoderStream.getLameSampleRate(44100));
    }
}