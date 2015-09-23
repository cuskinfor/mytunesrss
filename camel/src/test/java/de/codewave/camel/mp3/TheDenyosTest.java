/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.mp3.framebody.v2.PICFrameBody;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * de.codewave.camel.mp3.TheDenyosTest
 */
public class TheDenyosTest {

    private Id3Tag myTag;

    @Before
    public void setUp() throws Exception {
        myTag = Mp3Utils.readId3Tag(getClass().getResource("/TheDenyos.mp3"));
    }

    @Test
    public void testVersion() {
        assertEquals("ID3v2.2.0", myTag.getLongVersionIdentifier());
    }

    @Test
    public void testAlbum() {
        assertEquals("The Denyos", myTag.getAlbum());
    }

    @Test
    public void testArtist() {
        assertEquals("Denyo", myTag.getArtist());
    }

    @Test
    public void testTitle() {
        assertEquals("The Denyos", myTag.getTitle());
    }

    @Test
    public void testTrackNumber() {
        assertEquals(1, ((Id3v2Tag)myTag).getTrackNumber());
    }

    @Test
    public void testImage() {
        assertEquals("image/jpg", new PICFrameBody(((Id3v2Tag)myTag).getFrame("PIC")).getMimeType().toLowerCase());
    }

    @Test
    public void testGenre() {
        assertNull(myTag.getGenreAsString());
    }
}