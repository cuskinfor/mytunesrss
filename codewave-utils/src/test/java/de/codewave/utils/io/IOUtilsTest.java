package de.codewave.utils.io;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;

/**
 * de.codewave.utils.io.IOUtilsTest
 */
public class IOUtilsTest extends TestCase {

    public static Test suite() {
        return new TestSuite(IOUtilsTest.class);
    }

    public IOUtilsTest(String name) {
        super(name);
    }

    public void testIsContained() throws IOException {
        File dir = new File("src/test/java");
        assertTrue(IOUtils.isContained(dir, new File("src/test/java/de/codewave/utils/io/IOUtilsTest.java")));
        assertFalse(IOUtils.isContained(dir, new File("src/test/java/de/codewave/utils/io/IOUtilsTest.dummy")));
        assertFalse(IOUtils.isContained(dir, new File("src/main/java/de/codewave/utils/io/IOUtils.java")));
        assertFalse(IOUtils.isContained(dir, new File("src/test/java")));
        File temp1 = File.createTempFile("pre-", "-post");
        temp1.delete();
        temp1.mkdir();
        File temp2 = new File(temp1.getAbsolutePath() + " (2)");
        temp2.mkdir();
        assertFalse(IOUtils.isContained(temp1, temp2));
        temp1.delete();
        temp2.delete();
    }

    public void testGetFileIdentifier() throws IOException {
        File dir = new File(".");
        File file = new File("pom.xml");
        assertEquals(new String(Base64.encodeBase64((System.getProperty("file.separator") + "pom.xml").getBytes("UTF-8")), "UTF-8"), IOUtils.getFileIdentifier(dir, file));
    }
}