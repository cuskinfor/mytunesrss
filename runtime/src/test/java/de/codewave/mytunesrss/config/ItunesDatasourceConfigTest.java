package de.codewave.mytunesrss.config;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class ItunesDatasourceConfigTest {

    @Test
    public void testAutoAddFolder() throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
        File tempFolder = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
        try {
            ItunesDatasourceConfig itunesDatasourceConfig = new ItunesDatasourceConfig("test", new File(getClass().getResource("/itunes.xml").toURI()).getAbsolutePath());
            tempFolder.mkdir();
            File testAutoAddFolder = new File(tempFolder, "Automatically Add to iTunes.localized");
            itunesDatasourceConfig.addPathReplacement(new ReplacementRule("/Users/mdescher/Music/iTunes/iTunes Media", tempFolder.getAbsolutePath()));
            assertNull(itunesDatasourceConfig.getAutoAddToItunesFolder());
            testAutoAddFolder.mkdir();
            assertEquals(testAutoAddFolder.getAbsolutePath(), itunesDatasourceConfig.getAutoAddToItunesFolder().getAbsolutePath());
        } finally {
            FileUtils.deleteDirectory(tempFolder);
        }
    }

}
