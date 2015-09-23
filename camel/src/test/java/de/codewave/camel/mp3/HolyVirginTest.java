package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.Mp3Exception;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class HolyVirginTest {
    private Id3Tag myTag;

    @Before
    public void before() throws Exception {
        myTag = Mp3Utils.readId3Tag(getClass().getResource("/HolyVirgin.mp3"));
    }

    @Test
    public void testVersion() {
        assertEquals("ID3v2.3.0", myTag.getLongVersionIdentifier());
    }

    @Test
    public void testAlbum() {
        assertEquals("Future Trance Vol 33", myTag.getAlbum());
    }

    @Test
    public void testArtist() {
        assertEquals("Groove Coverage", myTag.getArtist());
    }

    @Test
    public void testTitle() {
        assertEquals("Holy Virgin", myTag.getTitle());
    }

    @Test
    public void testTrackNumber() {
        assertEquals(0, ((Id3v2Tag) myTag).getTrackNumber());
    }

    @Test
    public void testEstimatedDuration() throws IOException, Mp3Exception {
        Mp3Info mp3Info = Mp3Utils.getMp3Info(getClass().getResourceAsStream("/HolyVirgin.mp3"));
        assertEquals(0, mp3Info.getDurationSeconds());
        assertEquals(true, mp3Info.isVbr());
        assertEquals(128000, mp3Info.getMinBitrate());
        assertEquals(192000, mp3Info.getMaxBitrate());
        assertEquals(192000, mp3Info.getAvgBitrate());
        assertEquals(44100, mp3Info.getMinSampleRate());
        assertEquals(44100, mp3Info.getMaxSampleRate());
        assertEquals(44100, mp3Info.getAvgSampleRate());
    }

}