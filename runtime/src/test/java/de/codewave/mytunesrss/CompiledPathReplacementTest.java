package de.codewave.mytunesrss;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * de.codewave.mytunesrss.CompiledPathReplacementTest
 */
public class CompiledPathReplacementTest {

    @Test
    public void testReplace() {
        PathReplacement replacement = new PathReplacement("c:/.*?/iTunes Music/(.*)", "/backup/iTunes Music/$1");
        CompiledPathReplacement compiledPathReplacement = new CompiledPathReplacement(replacement);
        assertEquals("/backup/iTunes Music/My Artist/My Album/My Track.mp3", compiledPathReplacement.replace("c:/Documents and Settings/mdescher/My Music/iTunes/iTunes Music/My Artist/My Album/My Track.mp3"));
        assertEquals("/no match", compiledPathReplacement.replace("/no match"));
    }
}