package de.codewave.mytunesrss;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * de.codewave.mytunesrss.CompiledReplacementRuleTest
 */
public class CompiledReplacementRuleTest {

    @Test
    public void testReplace() {
        ReplacementRule replacementRule = new ReplacementRule("c:/.*?/iTunes Music/(.*)", "/backup/iTunes Music/$1");
        CompiledReplacementRule compiledReplacementRule = new CompiledReplacementRule(replacementRule);
        assertEquals("/backup/iTunes Music/My Artist/My Album/My Track.mp3", compiledReplacementRule.replace("c:/Documents and Settings/mdescher/My Music/iTunes/iTunes Music/My Artist/My Album/My Track.mp3"));
        assertEquals("/no match", compiledReplacementRule.replace("/no match"));
        replacementRule = new ReplacementRule("iTunes Music", "backup/iTunes Music");
        compiledReplacementRule = new CompiledReplacementRule(replacementRule);
        assertEquals("C:/backup/iTunes Music/My Artist/My Album/My Track.mp3", compiledReplacementRule.replace("C:/iTunes Music/My Artist/My Album/My Track.mp3"));
    }
}