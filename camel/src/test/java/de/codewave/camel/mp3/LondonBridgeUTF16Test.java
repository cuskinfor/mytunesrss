package de.codewave.camel.mp3;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * de.codewave.camel.mp3.TheDenyosTest
 */
public class LondonBridgeUTF16Test {
    private Id3Tag myTag;


    @Before
    public void setUp() throws Exception {
        myTag = Mp3Utils.readId3Tag(getClass().getResource("/LondonBridge_UTF16.mp3"));
    }

    @Test
    public void testVersion() {
        assertEquals("ID3v2.3.0", myTag.getLongVersionIdentifier());
    }

    @Test
    public void testAlbum() {
        assertEquals("The Dutchess", myTag.getAlbum());
    }

    @Test
    public void testArtist() {
        assertEquals("Fergie", myTag.getArtist());
    }

    @Test
    public void testTitle() {
        assertEquals("London Bridge", myTag.getTitle());
    }

    @Test
    public void testTrackNumber() {
        assertEquals(4, ((Id3v2Tag)myTag).getTrackNumber());
    }
}