/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.Mp3Exception;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * de.codewave.camel.mp3.TheDenyosTest
 */
public class ItsMyLifeTest {

    private Id3Tag myTag;

    @Before
    public void setUp() throws Exception {
        myTag = Mp3Utils.readId3Tag(getClass().getResource("/ItsMyLife.mp3"));
    }

    @Test
    public void testVersion() {
        assertEquals("ID3v2.4.0", myTag.getLongVersionIdentifier());
    }

    @Test
    public void testAlbum() {
        assertNull(myTag.getAlbum());
    }

    @Test
    public void testArtist() {
        assertEquals("Bon Jovi", myTag.getArtist());
    }

    @Test
    public void testTitle() {
        assertEquals("Its My Life", myTag.getTitle());
    }

    @Test
    public void testTrackNumber() {
        assertEquals(0, ((Id3v2Tag)myTag).getTrackNumber());
    }

    @Test
    public void testGenre() {
        assertEquals("Rock", myTag.getGenreAsString());
    }

    @Test
    public void testEstimatedDuration() throws IOException, Mp3Exception {
        Mp3Info mp3Info = Mp3Utils.getMp3Info(getClass().getResourceAsStream("/ItsMyLife.mp3"));
        Assert.assertEquals(3, mp3Info.getDurationSeconds());
        Assert.assertEquals(false, mp3Info.isVbr());
        Assert.assertEquals(192000, mp3Info.getMinBitrate());
        Assert.assertEquals(192000, mp3Info.getMaxBitrate());
        Assert.assertEquals(192000, mp3Info.getAvgBitrate());
        assertEquals(44100, mp3Info.getMinSampleRate());
        assertEquals(44100, mp3Info.getMaxSampleRate());
        assertEquals(44100, mp3Info.getAvgSampleRate());
    }
}