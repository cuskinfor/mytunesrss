/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp4.MoovAtom;
import de.codewave.camel.mp4.Mp4Parser;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * de.codewave.camel.mp3.Mp3FileTest
 */
public class Mp3FileTest {

    @Test
    public void testHungUp() throws IOException, IllegalHeaderException {
        System.out.println("File: HungUp.mp3");
        displayTag(Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("HungUp.mp3")));
    }

    @Test
    public void testKnowYourRights() throws IOException, IllegalHeaderException {
        System.out.println("File: KnowYourRights.mp3");
        displayTag(Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("KnowYourRights.mp3")));
    }

    @Test
    public void testTheOtherWay() throws IOException, IllegalHeaderException {
        System.out.println("File: TheOtherWay.mp3");
        displayTag(Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("TheOtherWay.mp3")));
    }

    @Test
    public void testBadman() throws IOException, IllegalHeaderException {
        System.out.println("File: Badman.mp3");
        displayTag(Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("Badman.mp3")));
    }

    @Test
    public void testGirlLikeMe() throws IOException, IllegalHeaderException {
        System.out.println("File: GirlLikeMe.mp3");
        displayTag(Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("GirlLikeMe.mp3")));
    }

    @Test
    public void testHP2007mysteriekammeretcd0107() throws IOException, IllegalHeaderException {
        System.out.println("File: HP2007-mysteriekammeret-cd01-07.mp3");
        displayTag(Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("HP2007-mysteriekammeret-cd01-07.mp3")));
    }

    private void displayTag(Id3Tag tag) {
        System.out.println(tag);
    }

    @Test
    public void testWasMusstIchHoeren() throws IOException, IllegalHeaderException {
        Id3v2Tag tag = (Id3v2Tag) Mp3Utils.readId3Tag(getClass().getClassLoader().getResource("Was_musst_ich_hoeren.mp3"));
        assertNotNull(tag);
        assertEquals("Der Fliegende Hollander", tag.getAlbum());
        assertEquals("Richard Wagner", tag.getAlbumArtist());
        assertEquals("Wagner - Franz Konwitschny, Staatskapelle Berlin, Dieskau, Frick, Schech, Schock, Wunderlich", tag.getArtist());
        assertEquals("Richard Wagner", tag.getComposer());
        assertEquals("Classical", tag.getGenreAsString());
        assertEquals("Was muBt' ich horen {Nr.8 Finale}", tag.getTitle());
    }
}