package de.codewave.mytunesrss;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MyTunesRssBase64UtilsTest {

    @Test
    public void testBase64() {
        assertEquals("bWRlc2NoZXI", MyTunesRssBase64Utils.encode("mdescher"));
        assertEquals("mdescher", MyTunesRssBase64Utils.decodeToString(MyTunesRssBase64Utils.encode("mdescher")));
    }
}
