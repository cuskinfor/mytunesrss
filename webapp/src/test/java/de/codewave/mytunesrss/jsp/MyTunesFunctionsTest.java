package de.codewave.mytunesrss.jsp;

import de.codewave.mytunesrss.MyTunesRss;
import static junit.framework.Assert.assertEquals;
import org.junit.Test;

/**
 * de.codewave.mytunesrss.jsp.MyTunesFunctionsTest
 */
public class MyTunesFunctionsTest {

    @Test
    public void testMakeHttp() {
        MyTunesRss.CONFIG.setPort(8080);
        MyTunesRss.CONFIG.setSslPort(8443);
        MyTunesRss.CONFIG.setTomcatProxyHost(null);
        MyTunesRss.CONFIG.setTomcatProxyPort(0);
        assertEquals("http://localhost:8080/mytunesrss", MyTunesFunctions.makeHttp("https://localhost:8443/mytunesrss"));
        assertEquals("http://localhost:8080", MyTunesFunctions.makeHttp("https://localhost:8443"));
        MyTunesRss.CONFIG.setTomcatProxyHost("codewave-test");
        MyTunesRss.CONFIG.setTomcatProxyPort(80);
        assertEquals("http://codewave-test:80/mytunesrss", MyTunesFunctions.makeHttp("https://localhost:8443/mytunesrss"));
        assertEquals("http://codewave-test:80", MyTunesFunctions.makeHttp("https://localhost:8443"));
    }

}