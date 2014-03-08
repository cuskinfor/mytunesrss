/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NetworkTest {

    @Test
    public void testParse() {
        new Network("192.168.0.3/8");
        new Network("192.168.0.3/16");
        new Network("192.168.0.3/24");
        new Network("192.168.0.3/32");
        new Network("192.168.0.3");
        new Network("");
        new Network(null);
    }

    @Test
    public void testMatches() {
        assertTrue(new Network("192.0.0.0/8").matches("192.168.10.13"));
        assertTrue(new Network("192.0.0.0/8").matches("192.234.23.34"));
        assertTrue(new Network("192.0.0.0/8").matches("192.111.123.53"));
        assertTrue(new Network("192.168.0.0/16").matches("192.168.10.13"));
        assertTrue(new Network("192.168.0.0/16").matches("192.168.23.34"));
        assertTrue(new Network("192.168.0.0/16").matches("192.168.123.53"));
        assertTrue(new Network("192.168.0.3/32").matches("192.168.0.3"));
        assertTrue(new Network("192.168.0.3").matches("192.168.0.3"));
        assertTrue(new Network(null).matches("192.168.0.3"));
        assertTrue(new Network(null).matches("0.0.0.0"));
        assertTrue(new Network(null).matches("255.255.255.255"));
        assertTrue(new Network(null).matches("1.2.3.4"));

        assertFalse(new Network("0.0.0.0/32").matches("1.2.3.4"));
        assertFalse(new Network("0.0.0.0/32").matches("192.168.0.2"));
        assertFalse(new Network("0.0.0.0/32").matches("255.234.234.3"));
    }
}
