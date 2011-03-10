/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class MyTunesRssFileProcessorTest {

    @BeforeClass
    public static void beforeClass() {
        MyTunesRss.CONFIG = new MyTunesRssConfig();
    }

    private MyTunesRssFileProcessor myProcessor;
    private File myFile;

    @Before
    public void before() throws URISyntaxException {
        myProcessor = new MyTunesRssFileProcessor(null, null, 0, null);
        myFile = new File(getClass().getResource("/de/codewave/mytunesrss/MyTunesRss.class").toURI());
    }

    @Test
    public void testGetFallbackNamesFile() {
        assertEquals("this MyTunesRss.class name", myProcessor.getFallbackName(myFile, "this [[[file]]] name"));
        assertEquals("this MyTunesRss name", myProcessor.getFallbackName(myFile, "this [[[file:(.*)\\.class]]] name"));
    }

    @Test
    public void testGetFallbackNamesDir() {
        assertEquals("this mytunesrss name", myProcessor.getFallbackName(myFile, "this [[[dir:0]]] name"));
        assertEquals("this codewave name", myProcessor.getFallbackName(myFile, "this [[[dir:1]]] name"));
        assertEquals("this wave name", myProcessor.getFallbackName(myFile, "this [[[dir:1:code(.*)]]] name"));
    }

}
