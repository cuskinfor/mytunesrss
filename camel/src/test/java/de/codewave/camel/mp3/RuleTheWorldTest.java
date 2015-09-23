/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * de.codewave.camel.mp3.TheDenyosTest
 */
public class RuleTheWorldTest {
    private Id3v2Tag myTag;

    @Before
    public void setUp() throws Exception {
        myTag = (Id3v2Tag)Mp3Utils.readId3Tag(getClass().getResource("/RuleTheWorld.mp3"));
    }

    @Test
    public void testVersion() {
        assertEquals("ID3v2.3.0", myTag.getLongVersionIdentifier());
    }

    @Test
    public void testAlbum() {
        assertEquals("Bravo The Hits 2007", myTag.getAlbum());
    }

    @Test
    public void testArtist() {
        assertEquals("Take That", myTag.getArtist());
    }

    @Test
    public void testTitle() {
        assertEquals("Rule the World", myTag.getTitle());
    }

    @Test
    public void testTrackNumber() {
        assertEquals(14, myTag.getTrackNumber());
    }

    @Test
    public void testGenre() {
        assertEquals("Pop", myTag.getGenreAsString());
    }

    @Test
    public void testComment() {
        assertEquals("one group to rule them all", myTag.getFrameBodyToString("COM", "COMM"));
    }
}