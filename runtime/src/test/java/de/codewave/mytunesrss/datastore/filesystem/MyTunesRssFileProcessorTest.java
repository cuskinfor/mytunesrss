/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.datastore.filesystem;

import de.codewave.mytunesrss.MyTunesRssTestUtils;
import de.codewave.mytunesrss.config.WatchfolderDatasourceConfig;
import de.codewave.mytunesrss.datastore.updatequeue.DatabaseUpdateQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

public class MyTunesRssFileProcessorTest {

    private MyTunesRssFileProcessor myProcessor;
    private File myFile;

    @Before
    public void before() throws URISyntaxException, SQLException, IOException, ClassNotFoundException, NoSuchAlgorithmException {
        MyTunesRssTestUtils.before();
        myProcessor = new MyTunesRssFileProcessor(new WatchfolderDatasourceConfig("id1", "dummy1"), new DatabaseUpdateQueue(2500), 0, null, null);
        myFile = new File(getClass().getResource("/de/codewave/mytunesrss/MyTunesRss.class").toURI());
    }

    @After
    public void after() {
        MyTunesRssTestUtils.after();
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
