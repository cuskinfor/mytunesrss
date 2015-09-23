package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ManfredMannApostropheTest {
    
    @Test
    public void testWindows1252Parsing() throws IOException, IllegalHeaderException {
        Id3v2Tag tag = (Id3v2Tag) Mp3Utils.readId3Tag(getClass().getResource("/203-manfred_mannaes_earth_band-blinded_by_the_light.mp3"));
        assertEquals("MANFRED MANN\u2019S EARTH BAND", tag.getArtist());
    }
    
}
