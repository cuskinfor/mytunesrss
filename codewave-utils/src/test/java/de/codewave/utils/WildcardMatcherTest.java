package de.codewave.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WildcardMatcherTest {
    @Test
    public void testMatches() throws Exception {
        String test = "The quick brown fox jumps over the lazy dog";

        assertTrue(new WildcardMatcher("*").matches(test));
        assertTrue(new WildcardMatcher("*brown*lazy*").matches(test));
        assertTrue(new WildcardMatcher("*quick*fox*over*lazy*dog*").matches(test));
        assertTrue(new WildcardMatcher("*brown fox jumps*").matches(test));
        assertTrue(new WildcardMatcher("The *").matches(test));
        assertTrue(new WildcardMatcher("* dog").matches(test));
        assertTrue(new WildcardMatcher("* brown fox *").matches(test));
        assertTrue(new WildcardMatcher("The * dog").matches(test));

        assertFalse(new WildcardMatcher("x*").matches(test));
        assertFalse(new WildcardMatcher("*x").matches(test));
        assertFalse(new WildcardMatcher("*whale*").matches(test));
    }

    @Test
    public void testMatchesWithEscape() throws Exception {
        String test = "Test * \\ chars";

        assertTrue(new WildcardMatcher("Test \\* *").matches(test));
        assertTrue(new WildcardMatcher("*\\\\*").matches(test));
        assertTrue(new WildcardMatcher("\\T\\e\\s\\t*").matches(test));
        assertTrue(new WildcardMatcher("*char\\s").matches(test));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPattern() {
        new WildcardMatcher("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPattern() {
        new WildcardMatcher(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalEscapePattern() {
        new WildcardMatcher("Illegal Escape\\");
    }
}
