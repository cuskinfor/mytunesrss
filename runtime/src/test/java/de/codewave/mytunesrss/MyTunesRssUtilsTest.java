package de.codewave.mytunesrss;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class MyTunesRssUtilsTest {
    
    @Test
    public void testSubstringsBetween() {
        assertTokens(MyTunesRssUtils.substringsBetween("aa [[[salt]]] bb [[[sugar]]] cc", "[[[", "]]]"), "salt", "sugar");
        assertTokens(MyTunesRssUtils.substringsBetween("[[[salt]]][[[sugar]]]", "[[[", "]]]"), "salt", "sugar");
        assertTokens(MyTunesRssUtils.substringsBetween("[[[[salt]]]][[[[sugar]]]]", "[[[", "]]]"), "[salt]", "[sugar]");
        assertTokens(MyTunesRssUtils.substringsBetween("a [[[[salt]]]] b [[[[sugar]]]] c", "[[[", "]]]"), "[salt]", "[sugar]");
        assertTokens(MyTunesRssUtils.substringsBetween("a [[[[salt]]]] b [[[[sugar]]", "[[[", "]]]"), "[salt]");
        assertTokens(MyTunesRssUtils.substringsBetween("a [[[[salt]]]] b [[sugar]]]]", "[[[", "]]]"), "[salt]");
        assertTokens(MyTunesRssUtils.substringsBetween("[[[salt]]]", "[[[", "]]]"), "salt");
        assertTokens(MyTunesRssUtils.substringsBetween("", "[[[", "]]]"));
        assertTokens(MyTunesRssUtils.substringsBetween(null, "[[[", "]]]"));
        assertTokens(MyTunesRssUtils.substringsBetween("[[[salt]]]", null, "]]]"));
        assertTokens(MyTunesRssUtils.substringsBetween("[[[salt]]]", "[[[", null));
    }

    private void assertTokens(String[] actual, String... expected) {
        assertArrayEquals(expected, actual);
    }

}
