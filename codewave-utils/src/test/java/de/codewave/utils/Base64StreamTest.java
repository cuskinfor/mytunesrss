package de.codewave.utils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;

/**
 * de.codewave.utils.Base64StreamTest
 */
public class Base64StreamTest extends TestCase {
    public static Test suite() {
        return new TestSuite(Base64StreamTest.class);
    }

    public Base64StreamTest(String name) {
        super(name);
    }

    public void testOutputStream() throws IOException {
        String original = "Welcome to the Codewave Utils Base64Writer Test Case. All this crazy stuff is copyright by Michael Descher";
        String encoded = new String(Base64.encodeBase64(original.getBytes("UTF-8")), "UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Base64OutputStream stream = new Base64OutputStream(baos);
        stream.setEncoding("UTF-8");
        stream.write(original.getBytes("UTF-8"));
        stream.close();
        assertEquals(encoded, baos.toString("UTF-8"));
    }

    public void testOutputStreamLineLength() throws IOException {
        String original = "codewave-software!codewave-software!codewave-software!";
        String encoded = new String(Base64.encodeBase64("codewave-software!".getBytes("UTF-8")), "UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Base64OutputStream stream = new Base64OutputStream(baos);
        stream.setEncoding("UTF-8");
        stream.setLineLength("codewave-software!".length() * 4 / 3);
        stream.write(original.getBytes("UTF-8"));
        stream.close();
        assertEquals(encoded + System.getProperty("line.separator") + encoded + System.getProperty("line.separator") + encoded, baos.toString("UTF-8"));
    }

    public void testInputStream() throws IOException {
        byte[] original = new byte[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        String encoded = new String(Base64.encodeBase64(original), "UTF-8");
        byte[] decoded = new byte[20];
        InputStream inputStream = new Base64InputStream(new StringReader(encoded));
        int count  = inputStream.read(decoded);
        assertEquals("Wrong byte count", original.length, count);
        byte[] decodedTrimmed = new byte[original.length];
        System.arraycopy(decoded, 0, decodedTrimmed, 0, decodedTrimmed.length);
        assertTrue("Wrong data", Arrays.equals(original, decodedTrimmed));
    }
}