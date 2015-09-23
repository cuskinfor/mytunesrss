/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * de.codewave.camel.mp3.ScanMp3Test
 */
public class GenreTest {

    @Test
    public void testGirlLikeMe() throws IOException, IllegalHeaderException {
        assertEquals("R&B", Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("GirlLikeMe.mp3")).getGenreAsString());
    }

    @Test
    public void testStingFragile() throws IOException, IllegalHeaderException {
        assertEquals("Pop", Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("Sting_Fragile.mp3")).getGenreAsString());
    }
}