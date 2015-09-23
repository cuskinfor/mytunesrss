package de.codewave.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class MiscUtilsTest {

    @Test
    public void testSplitString() {
        List<String> splitted = MiscUtils.splitString("abcdefgh", 3);
        assertEquals("abc", splitted.get(0));
        assertEquals("def", splitted.get(1));
        assertEquals("gh", splitted.get(2));
    }

    @Test
    public void testGetUtf8Bytes() {
        assertArrayEquals(new byte[]{(byte)0xc3, (byte)0xa9}, MiscUtils.getUtf8Bytes("é"));
    }

    @Test
    public void testGetUtf8String() {
        assertEquals("é", MiscUtils.getUtf8String(new byte[] {(byte)0xc3, (byte)0xa9}));
    }

    @Test
    public void testGetUtf8UrlEncoded() {
        assertEquals("test%20%C3%A9", MiscUtils.getUtf8UrlEncoded("test é"));
    }

    @Test
    public void testGetUtf8UrlDecoded() {
        assertEquals("test é", MiscUtils.getUtf8UrlDecoded("test%20%C3%a9"));
    }

    @Test
    public void testCompose() {
        assertArrayEquals(new byte[]{(byte)0xc3, (byte)0xa9}, MiscUtils.getUtf8Bytes(MiscUtils.compose("é")));
    }

    @Test
    public void testDecompose() {
        assertArrayEquals(new byte[]{'e', (byte)0xcc, (byte)0x81}, MiscUtils.getUtf8Bytes(MiscUtils.decompose("é")));
    }

    @Test
    public void testCleanupToken() {
        assertEquals("test", MiscUtils.cleanupToken("test"));
        assertEquals(" test ", MiscUtils.cleanupToken(" test "));
        assertEquals("4711", MiscUtils.cleanupToken("0004711"));
        assertEquals("4711", MiscUtils.cleanupToken("4711"));
        assertEquals("0", MiscUtils.cleanupToken("0"));
        assertEquals("0", MiscUtils.cleanupToken("0000"));
    }

    @Test
    public void testTokenizeForNaturalSortString() {
        List<String> tokens = MiscUtils.tokenizeForNaturalSortString("0002haus 000123 affe 0000123 wal 000");
        Iterator<String> iter = tokens.iterator();
        Assert.assertEquals("0002haus ", iter.next());
        Assert.assertEquals("123", iter.next());
        Assert.assertEquals(" affe ", iter.next());
        Assert.assertEquals("123", iter.next());
        Assert.assertEquals(" wal ", iter.next());
        Assert.assertEquals("0", iter.next());
    }

    @Test
    public void testToNaturalSortString() {
        assertEquals("haus   123", MiscUtils.toNaturalSortString("haus 123", 10, 1));
        assertEquals("haus   123 affe 123", MiscUtils.toNaturalSortString("haus 123 affe 123", 10, 1));
        assertEquals("haus   123 affe  123", MiscUtils.toNaturalSortString("haus 123 affe 123", 10, 2));
        assertEquals("2fast  123 affe  123", MiscUtils.toNaturalSortString("2fast123 affe 123", 10, 2));
    }

    @Test
    public void testNaturalSort() {
        String[] items = {
                "Bravo Hits 1",
                "Bravo Hits 00010",
                "Bravo Hits   100",
                "Bravo Hits 2 (CD 2)",
                "Bravo Hits  2 (CD 01)",
                "Bravo Hits 020",
                "Bravo Hits       200",
                "Bravo Hits 0000000015"
        };
        List<String> naturals = new ArrayList<>();
        for (String item : items) {
            naturals.add(MiscUtils.toNaturalSortString(item, 255, 3));
        }
        Collections.sort(naturals);
        assertTrue(naturals.get(0).endsWith(" 1"));
        assertTrue(naturals.get(1).endsWith(" 1)"));
        assertTrue(naturals.get(2).endsWith(" 2)"));
        assertTrue(naturals.get(3).endsWith(" 10"));
        assertTrue(naturals.get(4).endsWith(" 15"));
        assertTrue(naturals.get(5).endsWith(" 20"));
        assertTrue(naturals.get(6).endsWith(" 100"));
        assertTrue(naturals.get(7).endsWith(" 200"));
    }

}
