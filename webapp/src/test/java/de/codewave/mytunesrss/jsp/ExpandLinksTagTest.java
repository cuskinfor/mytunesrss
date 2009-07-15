/*
 * ExpandLinksTagTest.java 15.07.2009
 * 
 * Copyright (c) 2009 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package de.codewave.mytunesrss.jsp;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExpandLinksTagTest {

    @Test
    public void testReplace() {
        assertEquals("here is a link to the <a href='hello kitty'>hello kitty</a> website!", ExpandLinksTag
                .replace("here is a link to the [a href='hello kitty']hello kitty[/a] website!"));
        assertEquals("<A HREF='hello kitty'>hello kitty</a>", ExpandLinksTag
                .replace("[A HREF='hello kitty']hello kitty[/a]"));
        assertEquals("<A    HREF = 'hello kitty'>hello kitty</a>", ExpandLinksTag
                .replace("[A    HREF = 'hello kitty']hello kitty[/a]"));
    }

    @Test
    public void testUnescaping() {
        assertEquals(
                "<a href='http://hello.kitty.de/survey?firstname=Max&lastname=Mustermann'>hello kitty</a>",
                ExpandLinksTag
                        .replace("[a href='http://hello.kitty.de/survey?firstname=Max&amp;lastname=Mustermann']hello kitty[/a]"));
        assertEquals("<a href='&<>&gt;'>hello kitty</a>", ExpandLinksTag
                .replace("[a href='&amp;&lt;&gt;&amp;gt;']hello kitty[/a]"));
    }

    @Test
    public void testTwoLinks() {
        assertEquals("we have <a href=\"1\">1</a> and <A href=\"2\">2</A> two links!", ExpandLinksTag
                .replace("we have [a href=\"1\"]1[/a] and [A href=\"2\"]2[/A] two links!"));
    }
}
