package de.codewave.utils.io;

import junit.framework.*;

import java.io.*;

/**
 * de.codewave.utils.io.FileCacheTest
 */
public class FileCacheTest extends TestCase {
    private FileCache myCache;
    private static final int ONE_HOUR = 60000;

    @Override
    protected void setUp() throws Exception {
        myCache = new FileCache("test", 100, 5);
        myCache.add("1", new File("1"), ONE_HOUR);
        myCache.add("2", new File("2"), 2 * ONE_HOUR);
        myCache.add("3", new File("3"), 3 * ONE_HOUR);
        myCache.add("4", new File("4"), 4 * ONE_HOUR);
    }

    public void testInitialContents() throws InterruptedException {
        Thread.sleep(500);
        assertNotNull(myCache.get("1"));
        assertNotNull(myCache.get("2"));
        assertNotNull(myCache.get("3"));
        assertNotNull(myCache.get("4"));
    }

    public void testExpiration() throws InterruptedException {
        myCache.add("x", new File("x"), 500);
        assertNotNull(myCache.get("x").getFile());
        Thread.sleep(700);
        assertNull(myCache.get("x"));
    }

    public void testLockUnlock() {
        myCache.lock("1");
        myCache.lock("1");
        myCache.unlock("1");
        myCache.unlock("1");
        try {
            myCache.unlock("1");
            fail("Locking exception expected.");
        } catch (Exception e) {
            // exception expected, so test case successful
        }
    }

    public void testTruncate() {
        myCache.truncate(2);
        assertNull(myCache.get("1"));
        assertNull(myCache.get("2"));
        assertNotNull(myCache.get("3"));
        assertNotNull(myCache.get("4"));
    }

    public void testDoNotRemoveNewFile() {
        myCache.add("n1", new File("n1"), Integer.MAX_VALUE);
        myCache.add("n2", new File("n2"), 10000);
        assertNotNull(myCache.get("n2"));
        assertNull(myCache.get("1"));
    }

    public void testUnlockAll() {
        myCache.lock("1");
        myCache.unlockAll();
        try {
            myCache.unlock("1");
            fail("Locking exception expected.");
        } catch (Exception e) {
            // exception expected, so test case successful
        }
    }
}
