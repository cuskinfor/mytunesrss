package de.codewave.mytunesrss.config;

import junit.framework.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class ItunesDatasourceConfigTest {

    @Test
    public void testAutoAddFolder() throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
        ItunesDatasourceConfig itunesDatasourceConfig = new ItunesDatasourceConfig("test", new File(getClass().getResource("/itunes.xml").toURI()).getAbsolutePath());
        Assert.assertEquals("/Users/mdescher/Music/iTunes/iTunes Media", itunesDatasourceConfig.getAutoAddToItunesFolder().getAbsolutePath());
        itunesDatasourceConfig.addPathReplacement(new ReplacementRule("/Users/mdescher/Music/", "/mnt/Music/"));
        Assert.assertEquals("/mnt/Music/iTunes/iTunes Media", itunesDatasourceConfig.getAutoAddToItunesFolder().getAbsolutePath());
    }

}
