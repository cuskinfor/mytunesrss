package de.codewave.utils;

import junit.framework.TestCase;

/**
 * de.codewave.utils.VersionTest
 */
public class VersionTest extends TestCase {
    public void testCompare() {
        assertTrue(new Version("1.0.0").compareTo(new Version("1.0.1")) < 0);
        assertTrue(new Version("1.0.0").compareTo(new Version("1.1.0")) < 0);
        assertTrue(new Version("1.0.0").compareTo(new Version("2")) < 0);
        assertTrue(new Version("1.0.0").compareTo(new Version("1")) == 0);
        assertTrue(new Version("1.0.0").compareTo(new Version("1.0")) == 0);
        assertTrue(new Version("1.0.0-SNAPSHOT").compareTo(new Version("1.0")) == 0);
        assertTrue(new Version("1.0.0-SNAPSHOT").compareTo(new Version("1.0.0-SNAPSHOT")) == 0);
        assertTrue(new Version("1.0.0-ALPHA").compareTo(new Version("1.0-BETA")) < 0);
        assertTrue(new Version("1.0.0-ALPHA-1").compareTo(new Version("1-ALPHA-2")) < 0);
        assertTrue(new Version("1.0.0-ALPHA-2").compareTo(new Version("1.0.1-ALPHA-1")) < 0);
        assertTrue(new Version("3.1.0-EAP-5").compareTo(new Version("3.1-EAP-5")) == 0);
        assertTrue(new Version("3.1.0-EAP-9").compareTo(new Version("3.1-EAP-10")) < 0);
    }

    public void testSplit() {
        assertEquals(2, new Version("2.3.4-EAP-2").getMajor());
        assertEquals(3, new Version("2.3.4-EAP-2").getMinor());
        assertEquals(4, new Version("2.3.4-EAP-2").getBugfix());
        assertEquals("EAP-2", new Version("2.3.4-EAP-2").getAppendix());
    }

    public void testToString() {
        assertEquals("2.3.4", new Version("2.3.4").toString());
        assertEquals("2.3.4-EAP-2", new Version("2.3.4-EAP-2").toString());
    }
}