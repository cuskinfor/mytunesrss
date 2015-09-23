/*
 * Copyright (c) 2005 Codewave Software. All Rights Reserved.
 */
package de.codewave.utils;

import junit.framework.*;

import java.util.*;

/**
 * de.codewave.utils.ProgramUtilsTest
 */
public class ProgramUtilsTest extends TestCase {
    public static Test suite() {
        return new TestSuite(ProgramUtilsTest.class);
    }

    public ProgramUtilsTest(String string) {
        super(string);
    }

    public void testGetCommandLineArguments() {
        Map<String, String[]> argMap = ProgramUtils.getCommandLineArguments(new String[] {"-input",
                                                                                          "inputName",
                                                                                          "-output",
                                                                                          "outputName",
                                                                                          "-debug",
                                                                                          "-values",
                                                                                          "10",
                                                                                          "20",
                                                                                          "30"});
        assertEquals("option count not correct", 4, argMap.keySet().size());
        // input
        assertTrue("option \"input\" not recognized", argMap.containsKey("input"));
        assertEquals("value count not correct for option \"input\"", 1, argMap.get("input").length);
        assertEquals("value not correct for option \"input\"", "inputName", argMap.get("input")[0]);
        // output
        assertTrue("option \"output\" not recognized", argMap.containsKey("output"));
        assertEquals("value count not correct for option \"output\"", 1, argMap.get("output").length);
        assertEquals("value not correct for option \"output\"", "outputName", argMap.get("output")[0]);
        // debug
        assertTrue("option \"debug\" not recognized", argMap.containsKey("debug"));
        assertEquals("value count not correct for option \"debug\"", 0, argMap.get("debug").length);
        // values
        assertTrue("option \"values\" not recognized", argMap.containsKey("values"));
        assertEquals("value count not correct for option \"values\"", 3, argMap.get("values").length);
        assertEquals("first value not correct for option \"values\"", "10", argMap.get("values")[0]);
        assertEquals("second value not correct for option \"values\"", "20", argMap.get("values")[1]);
        assertEquals("third value not correct for option \"values\"", "30", argMap.get("values")[2]);
        // test illegal first parameter
        assertNull("illegal first argument not detected", ProgramUtils.getCommandLineArguments(new String[] {
                "illegal", "-first", "parameter"}));
    }
}